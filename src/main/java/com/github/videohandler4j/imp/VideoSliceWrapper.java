package com.github.videohandler4j.imp;

import com.github.filehandler4j.imp.FileSliceWrapper;
import com.github.utils4j.IDurationProvider;
import com.github.videohandler4j.IVideoSlice;

public class VideoSliceWrapper extends FileSliceWrapper implements IVideoSlice {
  public VideoSliceWrapper(IVideoSlice slice) {
    super(slice);
  }

  protected final IVideoSlice slice() {
    return super.<IVideoSlice>getSlice();
  }
  
  @Override
  public long getTime() {
    return slice().getTime();
  }

  @Override
  public long getTime(IDurationProvider file) {
    return slice().getTime(file);
  }

  @Override
  public long end(IDurationProvider file) {
    return slice().end(file);
  }

  @Override
  public String startString() {
    return slice().startString();
  }

  @Override
  public String endString() {
    return slice().endString();
  }

  @Override
  public String timeString() {
    return slice().timeString();
  }

  @Override
  public String outputFileName() {
    return slice().outputFileName();
  }

  @Override
  public String outputFileName(IDurationProvider file) {
    return slice().outputFileName(file);
  }
}
