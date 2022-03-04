package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.toHmsString;

import com.github.filehandler4j.imp.DefaultFileSlice;
import com.github.utils4j.IDurationProvider;
import com.github.videohandler4j.IVideoSlice;

public final class DefaultVideoSlice extends DefaultFileSlice implements IVideoSlice {

  public DefaultVideoSlice() {
    this(0);
  }

  public DefaultVideoSlice(long startTime) {
    this(startTime, Long.MAX_VALUE);
  }
  
  public DefaultVideoSlice(long startTime, long endTime) {
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
  public long getTime(IDurationProvider file) {
    return end(file) - super.start();
  }

  @Override
  public long end(IDurationProvider file) {
    return Math.min(super.end(), file.getDuration().toMillis());
  }
  
  @Override
  public String outputFileName(IDurationProvider file) {
    return toHmsString(start()) + "_ate_" + toHmsString(end(file));
  }

  @Override
  public String endString() {
    long end = super.end();
    return Long.MAX_VALUE == end ? "__:__:__" : TimeTools.toString(end);
  }

  @Override
  public String startString() {
    return TimeTools.toString(start());
  }

  @Override
  public String timeString() {
    long end = super.end();
    return end == Long.MAX_VALUE ? "__:__:__" : TimeTools.toString(getTime());
  }
}
