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

import static com.github.utils4j.imp.Directory.stringPath;
import static com.github.utils4j.imp.Threads.startDaemon;
import static com.github.utils4j.imp.Throwables.runQuietly;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;

import com.github.filehandler4j.IInputFile;
import com.github.utils4j.IConstants;
import com.github.utils4j.gui.imp.ThrowableTracker;
import com.github.utils4j.imp.Strings;
import com.github.videohandler4j.IVideoInfoEvent;
import com.github.videohandler4j.imp.event.VideoInfoEvent;
import com.github.videohandler4j.imp.event.VideoOutputEvent;

import io.reactivex.Emitter;

class FFMPEGProcessor {

  private final File outputVideo;
  private final List<String> commandLine;
  private final Function<File, Boolean> accepter;
  
  FFMPEGProcessor(List<String> commandLine, File outputVideo) {
    this(commandLine, outputVideo, (f) -> true);
  }
  
  FFMPEGProcessor(List<String> commandLine, File outputVideo, Function<File, Boolean> accepter) {
    this.commandLine = commandLine;
    this.outputVideo = outputVideo;
    this.accepter = accepter;
  }
  
  private void interruptAndWait(Thread reader) throws InterruptedException {
    reader.interrupt();
    reader.join(3000);
  }
  
  final boolean proccess(IInputFile file, Emitter<IVideoInfoEvent> emitter) throws Exception {
    
    final String outputPath = stringPath(outputVideo);
    commandLine.add(outputPath);
    
    final Thread current = Thread.currentThread();
    final Process process = new ProcessBuilder(commandLine).redirectErrorStream(true).start();
        
    emitter.onNext(new VideoInfoEvent("Processing file: " + file.getName() + " output: " + outputPath));

    boolean success = false;
    boolean exit0 = false;
    
    try(InputStream input = process.getInputStream()) {
      Thread reader = startDaemon("ffmpeg output reader", () -> {
        Thread io = Thread.currentThread();
        try {
          BufferedReader br = new BufferedReader(new InputStreamReader(input, IConstants.CP_850));
          String inputLine;
          while (!io.isInterrupted() && (inputLine = br.readLine()) != null) {
            emitter.onNext(new VideoInfoEvent(Strings.replace(inputLine, '%', '#')));
          }
        } catch (Exception e) {
          emitter.onNext(new VideoInfoEvent("Fail in thread: " + io.getName() + ": " + e.getMessage()));
        }
      });

      try {
        try {
          exit0 = process.waitFor() == 0;
          success = exit0 && accepter.apply(outputVideo);
          interruptAndWait(reader);
          if (success) {
            emitter.onNext(new VideoOutputEvent("Generated file " + outputPath, outputVideo, file.length()));
          }
        } finally {
          runQuietly(process.destroyForcibly()::waitFor);
        }
      }catch(InterruptedException e) {
        try {
          interruptAndWait(reader);
        }finally {
          current.interrupt();
        }
        throw e;
      }finally {
        reader = null;
      }
    }finally {
      if (!success) {
        outputVideo.delete();
        if (!current.isInterrupted() && !exit0) {
          String message = "FFMPEG não processou este vídeo: " + file.getAbsolutePath() + "\n";
          String explainMessage = outputPath.length() >= 255 ? 
              "O caminho dos arquivos ultrapassa 256 caracteres. Tente diminuir o comprimento do nome do arquivo ou a hierarquia de pastas!" : 
              "Aparentemente o arquivo de vídeo não tem o formato aceito Mp4!";
          emitter.onNext(new VideoInfoEvent(message + explainMessage));
          throw new Exception(message + ThrowableTracker.DEFAULT.mark(explainMessage));
        }
      }
    }
    return success;
  }
}
