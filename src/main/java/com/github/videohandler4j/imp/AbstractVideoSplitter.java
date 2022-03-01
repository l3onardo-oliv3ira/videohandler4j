package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.VideoTool.FFMPEG;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.github.filehandler4j.IInputFile;
import com.github.filehandler4j.IIterator;
import com.github.filehandler4j.imp.AbstractFileRageHandler;
import com.github.filehandler4j.imp.ArrayIterator;
import com.github.utils4j.IConstants;
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

  private File currentOutput = null;
  private int bitRate = -1;

  public AbstractVideoSplitter() {
    this(new VideoSlice());
  }
  
  public AbstractVideoSplitter(IVideoSlice... ranges) {
    this(-1, ranges);
  }
  
  public AbstractVideoSplitter(int bitRate, IVideoSlice... ranges) {
    this(new ArrayIterator<IVideoSlice>(ranges));
    this.bitRate = bitRate;
  }
  
  public AbstractVideoSplitter(IIterator<IVideoSlice> iterator) {
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
    IVideoSlice next = next();
    
    if (next != null) {
      File ffmpegHome = FFMPEG.fullPath().orElseThrow(FFMpegNotFoundException::new).toFile();

      
      do {
        currentOutput = resolve(next.outputFileName(file));
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
        
        emitter.onNext(new VideoInfoEvent("Processando arquivo " + file.getName() + " saida: " + currentOutput.getAbsolutePath()));
        
        boolean success = false;

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
              emitter.onError(e);
            }
          });
          try {
            success = process.waitFor() == 0 && accept(currentOutput, next);
            reader.interrupt();
            reader.join(2000);
          }catch(InterruptedException e) {
            reader.interrupt();
            reader.join(2000);
            Thread.currentThread().interrupt();
            throw e;
          }finally {
            reader = null;
          }
        }finally {
          process.destroyForcibly();
          if (!success) {
            currentOutput.delete();
          } else {
            emitter.onNext(new VideoOutputEvent("Gerado arquivo", currentOutput, next.getTime(file)));
          }
        }
        next = next();
      }while(next != null);
    }      
  }

  protected boolean accept(File outputFile, IVideoSlice slice) {
    return true;
  }  
}
