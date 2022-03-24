package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.slices;

import java.time.Duration;

import com.github.utils4j.IDurationProvider;
import com.github.videohandler4j.IVideoFile;

public class ByDurationVideoSplitter extends AbstractVideoSplitter{
  
  private Duration maxSliceDuration;
  
  public ByDurationVideoSplitter(IDurationProvider file, Duration maxSliceDuration) {
    super(slices(file, maxSliceDuration, 0, DEFAULT_PREVIOUS_MARGING));
    this.maxSliceDuration = maxSliceDuration;
  }
  
  @Override
  protected boolean forceCopy(IVideoFile file) {
    return file.getDuration().toMillis() <= maxSliceDuration.toMillis();
  }
}
