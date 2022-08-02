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

import static com.github.utils4j.imp.Threads.startAsync;
import static com.github.utils4j.imp.Throwables.runQuietly;
import static com.github.videohandler4j.imp.VideoTools.FFMPEG;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.github.filehandler4j.IInputFile;
import com.github.filehandler4j.imp.AbstractFileHandler;
import com.github.utils4j.IConstants;
import com.github.utils4j.imp.Containers;
import com.github.videohandler4j.IVideoInfoEvent;
import com.github.videohandler4j.imp.event.VideoInfoEvent;
import com.github.videohandler4j.imp.event.VideoOutputEvent;
import com.github.videohandler4j.imp.exception.FFMpegNotFoundException;

import io.reactivex.Emitter;

public abstract class FFMPEGHandler extends AbstractFileHandler<IVideoInfoEvent> {

  @Override
  protected final void handle(IInputFile file, Emitter<IVideoInfoEvent> emitter) throws Exception {
    
    File ffmpegHome = FFMPEG.fullPath().orElseThrow(FFMpegNotFoundException::new).toFile();
    File outputVideo = resolveOutput(file.getShortName() + ".mp4");
    outputVideo.delete();

    List<String> commandLine = Containers.arrayList(
      ffmpegHome.getCanonicalPath(),
      "-y",
      "-i",
      file.getAbsolutePath(),
      "-stats_period",
      "1.5",
      "-hide_banner",
      "-nostdin"
    );
    fillParameters(commandLine);    
    commandLine.add(outputVideo.getCanonicalPath());
    
    final Thread current = Thread.currentThread();
    final Process process = new ProcessBuilder(commandLine).redirectErrorStream(true).start();
        
    emitter.onNext(new VideoInfoEvent("Processing file: " + file.getName() + " output: " + outputVideo.getAbsolutePath()));
    boolean success = false;

    try(InputStream input = process.getInputStream()) {
      Thread reader = startAsync("ffmpeg output reader", () -> {
        Thread io = Thread.currentThread();
        try {
          BufferedReader br = new BufferedReader(new InputStreamReader(input, IConstants.CP_850));
          String inputLine;
          while (!io.isInterrupted() && (inputLine = br.readLine()) != null) {
            emitter.onNext(new VideoInfoEvent(inputLine));
          }
        } catch (Exception e) {
          emitter.onNext(new VideoInfoEvent("Fail in thread: " + io.getName() + ": " + e.getMessage()));
        }
      });
      try {
        success = process.waitFor() == 0;
        interruptAndWait(reader);
        if (success) {
          emitter.onNext(new VideoOutputEvent("Generated file " + outputVideo, outputVideo, file.length()));
        }
      }catch(InterruptedException e) {
        runQuietly(process.destroyForcibly()::waitFor);
        if (!success) {
          interruptAndWait(reader);
          current.interrupt();
          throw e;
        }
      }
    }finally {
      if (!success) {
        outputVideo.delete();
        if (!current.isInterrupted()) {
          String message = "FFMPEG não processou este vídeo: " + file.getAbsolutePath();
          String outputPath = outputVideo.getCanonicalPath();
          if (outputPath.length() >= 255) {
            message = "\nO caminho dos arquivos ultrapassa 256 caracteres. Tente diminuir o "
                + "comprimento do nome do arquivo ou a hierarquia de pastas";
          }
          emitter.onNext(new VideoInfoEvent(message));
          throw new Exception(message);
        }
      }
    }
  }
  
  protected abstract void fillParameters(List<String> parameters);
}
