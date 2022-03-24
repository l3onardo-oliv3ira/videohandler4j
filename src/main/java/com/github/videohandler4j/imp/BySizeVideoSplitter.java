package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.slices;

import java.io.File;

import com.github.utils4j.imp.ArrayIterator;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;

public class BySizeVideoSplitter extends AbstractVideoSplitter{
  private final long maxSliceFileSize;
  private final IVideoFile file;
  private float percent = 0.95f; //5 percent discount
  
  public BySizeVideoSplitter(IVideoFile file, long maxSliceFileSize) {
    super(slices(file, maxSliceFileSize, 0, DEFAULT_PREVIOUS_MARGING));
    this.maxSliceFileSize = maxSliceFileSize;
    this.file = file;
  }
  
  @Override
  protected boolean forceCopy(IVideoFile file) {
    return file.length() <= maxSliceFileSize;
  }
  
  @Override
  protected boolean accept(File sliceFile, IVideoSlice slice) {
    if (sliceFile.length() <= maxSliceFileSize) {
      return true;
    }
    long smallerSize = (long)(percent * maxSliceFileSize);
    percent -= 0.05; //5 percent discount
    setIterator(new ArrayIterator<IVideoSlice>(slices(file, smallerSize, slice.start(), DEFAULT_PREVIOUS_MARGING)));
    return false;
  } 
}
