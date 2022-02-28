package com.github.videohandler4j.imp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.github.filehandler4j.imp.FileWrapper;
import com.github.utils4j.IConstants;
import com.github.utils4j.imp.Environment;
import com.github.utils4j.imp.Streams;
import com.github.utils4j.imp.Strings;
import com.github.utils4j.imp.function.Caller;
import com.github.videohandler4j.imp.exception.VideoDurationNotFound;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.imp.exception.FFMpegNotFoundException;

public enum VideoTool implements Caller<File, IVideoFile, VideoDurationNotFound> {
  FFMPEG("ffmpeg.exe");
  
  private final String fileName;
  
  private VideoTool(String fileName) {
    this.fileName = fileName;
  }
  
  public final Optional<Path> fullPath() {
    return Environment.pathFrom("FFMPEG_HOME", fileName);
  }

  @Override
  public IVideoFile call(File file) throws VideoDurationNotFound {
    try {
      Path ffmpeg = fullPath().orElseThrow(() -> new FFMpegNotFoundException());
      final Process process = new ProcessBuilder(
        ffmpeg.toFile().getAbsolutePath(),
        "-i",
        file.getAbsolutePath(),
        "-hide_banner"
      ).redirectErrorStream(true).start();
      
      String output = Strings.empty();
      try(InputStream input = process.getInputStream()) {
        output = Streams.readOutStream(input, IConstants.CP_850).get();
      }
      process.waitFor();
      final String durationPrefix = "Duration: ";
      int idx = output.indexOf(durationPrefix);
      if (idx < 0)
        throw new VideoDurationNotFound();
      int start = idx + durationPrefix.length();
      idx = output.indexOf(':', start);
      if (idx < 0)
        throw new VideoDurationNotFound();
      int hour = Strings.toInt(output.substring(start, idx), -1);
      if (hour < 0)
        throw new VideoDurationNotFound();
      start = idx + 1;
      idx = output.indexOf(':', start);
      if (idx < 0)
        throw new VideoDurationNotFound();
      int minutes = Strings.toInt(output.substring(start, idx), -1);
      if (hour < 0)
        throw new VideoDurationNotFound();
      start = idx + 1;
      idx = start;
      while(Character.isDigit(output.charAt(idx)))
        idx++;
      int seconds = Strings.toInt(output.substring(start, idx), -1);
      if (seconds < 0)
        throw new VideoDurationNotFound();
      long durationTime = hour * 3600 + minutes * 60000  + seconds * 1000;
      return new VideoFile(file, durationTime);
    } catch (VideoDurationNotFound e) {
      throw e;
    } catch (Exception e) {
      throw new VideoDurationNotFound(e);
    }
  }
  
  private static class VideoFile extends FileWrapper implements IVideoFile {

    private long durationTime;
    
    private VideoFile(File file, long durationTime) {
      super(file);
      this.durationTime = durationTime;
    }
    
    @Override
    public long getDuration() {
      return durationTime;
    }
  }
}
