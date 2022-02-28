package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.slice;

import java.io.File;

import com.github.filehandler4j.imp.ArrayIterator;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;

public class BySizeVideoSplitter extends AbstractVideoSplitter{
  private final long maxSize;
  private final IVideoFile file;
  private float percent = 0.95f;
  
  public BySizeVideoSplitter(IVideoFile file, long maxSize) {
    super(slice(file, maxSize));
    this.maxSize = maxSize;
    this.file = file;
  }
  
  @Override
  protected boolean accept(File outputFile, IVideoSlice slice) {
    if (outputFile.length() <= maxSize) {
      return true;
    }
    long newSize = (long)(percent * maxSize);
    percent -= 0.05;
    setIterator(new ArrayIterator<IVideoSlice>(slice(file, newSize, slice.start())));
    return false;
  } 
}
