package com.github.videohandler4j.imp;

import java.util.List;

import com.github.utils4j.imp.Args;

public class VideoOptimizer extends FFMPEGHandler {

  private final int crf;
  
  public VideoOptimizer() {
    this(40);
  }
  
  public VideoOptimizer(int crf) {
    this.crf = Args.requirePositive(crf, "crf <= 0");
  }
  
  @Override
  protected final void fillParameters(List<String> commandLine) {
    commandLine.add("-crf");
    commandLine.add(Integer.toString(crf));
    commandLine.add("-vf");
    commandLine.add("scale=trunc(iw/4)*2:trunc(ih/4)*2");
    commandLine.add("-max_muxing_queue_size");
    commandLine.add("89478485");
  }
}
