package com.github.videohandler4j.imp;

import com.github.videohandler4j.IVideoSlice;

public class BySliceVideoSplitter extends AbstractVideoSplitter{

  public BySliceVideoSplitter(IVideoSlice... slices) {
    super(slices);
  }
}
