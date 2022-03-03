package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.parse;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

import com.github.filehandler4j.imp.FileWrapper;
import com.github.utils4j.IConstants;
import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.Environment;
import com.github.utils4j.imp.Streams;
import com.github.utils4j.imp.Strings;
import com.github.utils4j.imp.function.Caller;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.imp.exception.FFMpegNotFoundException;
import com.github.videohandler4j.imp.exception.VideoDurationNotFound;

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
    Args.requireNonNull(file, "input is null");
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
      
      final int length = output.length();
      final int start = idx += durationPrefix.length();
      char chr;
      while(idx < length && (Character.isDigit(chr = output.charAt(idx)) || chr == ':'))
        idx++;

      String durationText = output.substring(start, idx);
      Duration duration = parse(durationText).orElseThrow(VideoDurationNotFound::new);
      return new VideoFile(file, duration);
    } catch (VideoDurationNotFound e) {
      throw e;
    } catch (Exception e) {
      throw new VideoDurationNotFound(e);
    }
  }
  
  private static class VideoFile extends FileWrapper implements IVideoFile {

    private final Duration duration;
    
    private VideoFile(File file, Duration duration) {
      super(file);
      this.duration = duration;
    }
    
    @Override
    public Duration getDuration() {
      return duration;
    }

    @Override
    public long getDuration(TemporalUnit unit) {
      return duration.get(unit);
    }
  }
}
