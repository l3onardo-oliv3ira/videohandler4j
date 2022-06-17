package com.github.videohandler4j.imp;

import static com.github.utils4j.imp.Strings.replace;
import static com.github.utils4j.imp.Threads.startAsync;
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

  protected abstract void fillParameters(List<String> parameters);
  
  @Override
  protected final void handle(IInputFile file, Emitter<IVideoInfoEvent> emitter) throws Exception {
    
    File ffmpegHome = FFMPEG.fullPath().orElseThrow(FFMpegNotFoundException::new).toFile();

    File currentOutput = resolveOutput(file.getShortName() + ".mp4");
    currentOutput.delete();

    List<String> commandLine = Containers.arrayList(
      ffmpegHome.getAbsolutePath(),
      "-y",
      "-i",
      file.getAbsolutePath(),
      "-stats_period",
      "1.5",
      "-hide_banner",
      "-nostdin"
    );
    
    fillParameters(commandLine);    
    
    commandLine.add(currentOutput.getCanonicalPath());
    
    commandLine.forEach(p -> System.out.print(" " + p));
    System.out.println();

    final Process process = new ProcessBuilder(commandLine).redirectErrorStream(true).start();
        
    emitter.onNext(new VideoInfoEvent("Processing file: " + file.getName() + " output: " + currentOutput.getAbsolutePath()));
        
    boolean success = false;

    try(InputStream input = process.getInputStream()) {
      Thread reader = startAsync("ffmpeg output reader", () -> {
        Thread currentThread = Thread.currentThread();
        try {
          BufferedReader br = new BufferedReader(new InputStreamReader(input, IConstants.CP_850));
          String inputLine;
          while (!currentThread.isInterrupted() && (inputLine = br.readLine()) != null) {
            emitter.onNext(new VideoInfoEvent(replace(inputLine, '%', '#')));
          }
        } catch (Exception e) {
          emitter.onNext(new VideoInfoEvent("Fail in thread: " + currentThread.getName() + ": " + e.getMessage()));
        }
      });
      try {
        if (success = process.waitFor() == 0) {
          emitter.onNext(new VideoOutputEvent("Generated file " + currentOutput, currentOutput, file.length()));
        };
        interruptAndWait(reader);
      }catch(InterruptedException e) {
        if (!success) {
          interruptAndWait(reader);
          Thread.currentThread().interrupt();
          throw e;
        }
      }finally {
        reader = null;
      }
    }finally {
      process.destroyForcibly();
      if (!success) {
        startAsync(currentOutput::delete, 3000); //delete file after delay
        if (!Thread.currentThread().isInterrupted()) {
          String outputPath = currentOutput.getCanonicalPath();
          String message = "FFMPEG não processou este vídeo: " + file.getAbsolutePath();
          if (outputPath.length() >= 255) {
            message = "\nO caminho dos arquivos ultrapassa 256 caracteres. Tente diminuir o comprimento do nome "
                + "do arquivo ou a hierarquia de pastas";
          }
          emitter.onNext(new VideoInfoEvent(message));
          throw new Exception(message);
        }
      }
    }
  }
}
