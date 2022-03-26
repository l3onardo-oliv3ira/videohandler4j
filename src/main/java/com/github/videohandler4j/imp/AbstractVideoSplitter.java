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
import java.util.List;

import com.github.filehandler4j.IInputFile;
import com.github.filehandler4j.imp.AbstractFileRageHandler;
import com.github.utils4j.IConstants;
import com.github.utils4j.IResetableIterator;
import com.github.utils4j.imp.ArrayIterator;
import com.github.utils4j.imp.Containers;
import com.github.utils4j.imp.States;
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
  private int bitRate = -1;

  public AbstractVideoSplitter() {
    this(new DefaultVideoSlice());
  }
  
  public AbstractVideoSplitter(IVideoSlice... ranges) {
    this(-1, ranges);
  }
  
  public AbstractVideoSplitter(int bitRate, IVideoSlice... ranges) {
    this(new ArrayIterator<IVideoSlice>(ranges));
    this.bitRate = bitRate;
  }
  
  public AbstractVideoSplitter(IResetableIterator<IVideoSlice> iterator) {
    super(iterator);
    this.reset();
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
  
  @Override
  protected void handle(IInputFile f, Emitter<IVideoInfoEvent> emitter) throws Exception {
    States.requireTrue(f instanceof IVideoFile, "file is not instance of VideoFile, please use VideoDescriptor instead");
    IVideoFile file = (IVideoFile)f;
    
    if (forceCopy(file)) {
      currentOutput = resolveOutput(file.getShortName() + "_" + new DefaultVideoSlice(0).outputFileName(file));
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
        
        currentOutput = resolveOutput(file.getShortName() + "_" + next.outputFileName(file));
        currentOutput.delete();
        
        List<String> commandLine = Containers.arrayList(
          ffmpegHome.getCanonicalPath(),
          "-y",
          "-i",
          file.getAbsolutePath(),
          "-threads",
          Long.toString(max(getRuntime().availableProcessors() - 1, 1)),
          "-hide_banner",
          "-ss",
          TimeTools.toString(next.start()),
          "-to",
          TimeTools.toString(next.end(file)),
          "-max_muxing_queue_size",
          "89478485"
        );
        
        if (bitRate > 0) {
          commandLine.add("-b:v");
          commandLine.add(bitRate + "k");
        }else {
          commandLine.add("-c");
          commandLine.add("copy");
        }
        commandLine.add(currentOutput.getAbsolutePath());

        final Process process = new ProcessBuilder(commandLine).redirectErrorStream(true).start();
        
        emitter.onNext(new VideoInfoEvent("Processing file: " + file.getName() + " output: " + currentOutput.getAbsolutePath()));
        
        boolean success = false;
        boolean splitSuccess = false;

        try(InputStream input = process.getInputStream()) {
          Thread reader = Threads.startAsync("ffmpeg output reader", () -> {
            Thread currentThread = Thread.currentThread();
            try {
              BufferedReader br = new BufferedReader(new InputStreamReader(input, IConstants.CP_850));
              String inputLine;
              while (!currentThread.isInterrupted() && (inputLine = br.readLine()) != null) {
                emitter.onNext(new VideoInfoEvent(inputLine));
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
          process.destroyForcibly();
          if (!success) {
            currentOutput.delete();
            if (!splitSuccess) {
              throw new Exception("FFMPEG não consegue dividir este vídeo: " + f.getAbsolutePath());
            }
          } else {
            emitter.onNext(new VideoOutputEvent("Generated file " + currentOutput, currentOutput, next.getTime(file)));
          }
        }
        
      }while((next = nextSlice()) != null);
    }      
  }

  protected boolean forceCopy(IVideoFile file) {
    return false;
  }

  private void interruptAndWait(Thread reader) throws InterruptedException {
    reader.interrupt();
    reader.join(2000);
  }

  protected boolean accept(File outputFile, IVideoSlice slice) {
    return true;
  }  
}
