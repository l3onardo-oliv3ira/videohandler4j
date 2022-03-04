package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.slices;

import java.time.Duration;

import com.github.utils4j.IDurationProvider;

public class ByDurationVideoSplitter extends AbstractVideoSplitter{

  public ByDurationVideoSplitter(IDurationProvider file, Duration maxSliceDuration) {
    super(slices(file, maxSliceDuration));
  }
}
