package com.github.videohandler4j.imp;

import java.util.List;

public class VideoOptimizer extends FFMPEGHandler {

  public VideoOptimizer() {
  }
  
  @Override
  protected final void fillParameters(List<String> commandLine) {
    commandLine.add("-crf");
    commandLine.add("40");
    commandLine.add("-vf");
    commandLine.add("\"scale=trunc(iw/4)*2:trunc(ih/4)*2\"");
    commandLine.add("-max_muxing_queue_size");
    commandLine.add("89478485");
  }
}
