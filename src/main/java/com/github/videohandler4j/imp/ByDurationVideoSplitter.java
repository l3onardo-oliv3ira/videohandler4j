package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.slices;

import java.time.Duration;

import com.github.videohandler4j.IVideoFile;

public class ByDurationVideoSplitter extends AbstractVideoSplitter{

  public ByDurationVideoSplitter(IVideoFile file, Duration maxSliceDuration) {
    super(slices(file, maxSliceDuration));
  }
}
