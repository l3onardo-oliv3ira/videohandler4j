package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.toHmsString;

import com.github.filehandler4j.imp.FileSlice;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;

public final class VideoSlice extends FileSlice implements IVideoSlice {

  public VideoSlice() {
    this(0, Long.MAX_VALUE);
  }

  public VideoSlice(long startTime, long endTime) {
    super(startTime, endTime);
  }

  @Override
  public long getTime() {
    return super.end() - super.start();
  }
  
  @Override
  public String outputFileName() {
    return toHmsString(start()) + "_ate_" + toHmsString(end());
  }

  @Override
  public long getTime(IVideoFile file) {
    return end(file) - super.start();
  }

  @Override
  public long end(IVideoFile file) {
    return Math.min(super.end(), file.getDuration().toMillis());
  }

  @Override
  public String outputFileName(IVideoFile file) {
    return toHmsString(start()) + "_ate_" + toHmsString(end(file));
  }
}
