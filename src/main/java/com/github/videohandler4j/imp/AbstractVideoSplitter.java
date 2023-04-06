/*
* MIT License
* 
* Copyright (c) 2022 Leonardo de Lima Oliveira
* 
* https://github.com/l3onardo-oliv3ira
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/


package com.github.videohandler4j.imp;

import static com.github.utils4j.gui.imp.ThrowableTracker.DEFAULT;
import static com.github.utils4j.imp.Directory.stringPath;
import static com.github.utils4j.imp.Throwables.runQuietly;
import static com.github.videohandler4j.imp.VideoTools.FFMPEG;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

import com.github.filehandler4j.IInputFile;
import com.github.filehandler4j.imp.AbstractFileRageHandler;
import com.github.utils4j.IConstants;
import com.github.utils4j.ISmartIterator;
import com.github.utils4j.imp.ArrayIterator;
import com.github.utils4j.imp.Containers;
import com.github.utils4j.imp.DurationTools;
import com.github.utils4j.imp.States;
import com.github.utils4j.imp.Strings;
import com.github.utils4j.imp.Threads;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoInfoEvent;
import com.github.videohandler4j.IVideoSlice;
import com.github.videohandler4j.imp.event.VideoInfoEvent;
import com.github.videohandler4j.imp.event.VideoOutputEvent;
import com.github.videohandler4j.imp.exception.FFMpegNotFoundException;

import io.reactivex.Emitter;

abstract class AbstractVideoSplitter extends AbstractFileRageHandler<IVideoInfoEvent, IVideoSlice> {

  protected static final long DEFAULT_PREVIOUS_MARGING = 0;// 3 * 1000; //three seconds previous
  
  private File currentOutput = null;
  private final boolean partPrefix;

  public AbstractVideoSplitter() {
    this(true);
  }
  
  public AbstractVideoSplitter(boolean partPrefix) {
    this(partPrefix, new DefaultVideoSlice());
  }
  
  public AbstractVideoSplitter(boolean partPrefix, IVideoSlice... ranges) {
    this(partPrefix, new ArrayIterator<IVideoSlice>(ranges));
  }
  
  public AbstractVideoSplitter(boolean partPrefix, ISmartIterator<IVideoSlice> iterator) {
    super(iterator);
    this.reset();
    this.partPrefix = partPrefix;
  }
  
  @Override
  protected void handleError(Throwable e) {    
    this.clearOutput();
    super.handleError(e);
  }

  private void clearOutput() {
    if (currentOutput != null) {
      currentOutput.delete();
      currentOutput = null;
    }
  }

  @Override
  public void reset() {
    currentOutput = null;
    super.reset();
  }  
  
  private String computeOutputFileName(IVideoFile inputFile, long sliceId, IVideoSlice videoSlice) {
    StringBuilder fileName = new StringBuilder();
    if (partPrefix)
      fileName.append("Parte " + Strings.padStart(sliceId,  2) + " - ");
    else
      fileName.append(inputFile.getShortName()).append('_');
    fileName.append(videoSlice.outputFileName(inputFile));
    if (partPrefix)
      fileName.append('_').append(inputFile.getShortName());
    return fileName.toString();
  }
  
  @Override
  protected void handle(IInputFile f, Emitter<IVideoInfoEvent> emitter) throws Exception {
    States.requireTrue(f instanceof IVideoFile, "file is not instance of VideoFile, please use VideoDescriptor instead");
    IVideoFile file = (IVideoFile)f;

    int sliceId = 1;
    
    if (forceCopy(file)) {
      currentOutput = resolveOutput(computeOutputFileName(file, sliceId, new DefaultVideoSlice(0)));
      try(OutputStream out = new FileOutputStream(currentOutput)) {
        Files.copy(file.toPath(), out);
      }
      emitter.onNext(new VideoOutputEvent("Generated file " + currentOutput, currentOutput, file.getDuration().toMillis()));
      return;
    }
    
    IVideoSlice next = nextSlice();
    
    
    if (next != null) {
      File ffmpegHome = FFMPEG.fullPath().orElseThrow(FFMpegNotFoundException::new).toFile();

      do {
        checkInterrupted();
        
        currentOutput = resolveOutput(computeOutputFileName(file, sliceId, next));
        
        currentOutput.delete();
        
        long start = next.start();

        Duration duration = Duration.ofMillis(next.end(file) - start);

        List<String> commandLine = Containers.arrayList(
          stringPath(ffmpegHome),
          "-y",
          "-nostdin",
          "-threads",
          Long.toString(max(getRuntime().availableProcessors() - 1, 1)),
          "-hide_banner",
          "-ss",
          DurationTools.toString(start) + ".000",          
          "-i",
          file.getAbsolutePath(),
          "-max_muxing_queue_size",
          "89478485"
        );
        
        commandLine.add("-codec");
        commandLine.add("copy");
        commandLine.add("-t");
        commandLine.add(Long.toString(duration.getSeconds()));

        final String outputPath = stringPath(currentOutput);
        
        commandLine.add(outputPath);

        final Process process = new ProcessBuilder(commandLine).redirectErrorStream(true).start();
        
        emitter.onNext(new VideoInfoEvent("Processing file: " + file.getName() + " output: " + outputPath));
        
        boolean success = false;
        boolean splitSuccess = false;

        try(InputStream input = process.getInputStream()) {
          Thread reader = Threads.startAsync("ffmpeg output reader", () -> {
            Thread currentThread = Thread.currentThread();
            try {
              BufferedReader br = new BufferedReader(new InputStreamReader(input, IConstants.CP_850));
              String inputLine;
              while (!currentThread.isInterrupted() && (inputLine = br.readLine()) != null) {
                emitter.onNext(new VideoInfoEvent(Strings.replace(inputLine, '%', '#')));
              }
            } catch (Exception e) {
              emitter.onNext(new VideoInfoEvent("Fail in thread: " + currentThread.getName() + ": " + e.getMessage()));
            }
          });
          try {
            splitSuccess = process.waitFor() == 0;
            success = splitSuccess && accept(currentOutput, next);
            interruptAndWait(reader);
          }catch(InterruptedException e) {
            interruptAndWait(reader);
            Thread.currentThread().interrupt();
            throw e;
          }finally {
            reader = null;
          }
        }finally {
          runQuietly(process.destroyForcibly()::wait);
          if (success) {
            sliceId++;
            emitter.onNext(new VideoOutputEvent("Generated file " + outputPath, currentOutput, next.getTime(file)));            
          } else {
            currentOutput.delete();
            if (!splitSuccess) {
              String message = "FFMPEG não pôde dividir este vídeo: " + f.getAbsolutePath() + "\n";
              String explainMessage = outputPath.length() >= 255 ? 
                  "O caminho dos arquivos ultrapassa 256 caracteres. Tente diminuir o comprimento do nome do arquivo ou da hierarquia de pastas!" :
                  "Aparentemente o arquivo de vídeo não tem o formato aceito Mp4!";
              emitter.onNext(new VideoInfoEvent(message + explainMessage));
              throw new Exception(message + DEFAULT.mark(explainMessage));
            }
          }
        }
      }while((next = nextSlice()) != null);
    }      
  }

  protected boolean forceCopy(IVideoFile file) {
    return false;
  }

  protected boolean accept(File outputFile, IVideoSlice slice) {
    return true;
  }  
}
