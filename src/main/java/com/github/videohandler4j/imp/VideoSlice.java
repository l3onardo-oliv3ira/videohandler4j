package com.github.videohandler4j.imp;

import com.github.filehandler4j.imp.FileRange;

public final class VideoSlice extends FileRange {

  public VideoSlice() {
    this(0, Long.MAX_VALUE);
  }

  public VideoSlice(long startTime, long endTime) {
    super(startTime, endTime);
  }
}
