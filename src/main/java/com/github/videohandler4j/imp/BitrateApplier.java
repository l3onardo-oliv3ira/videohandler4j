package com.github.videohandler4j.imp;

import java.util.List;

public class BitrateApplier extends FFMPEGHandler {

  private final int bitrate;

  public BitrateApplier(int bitrate) {
    this.bitrate = bitrate;
  }
  
  @Override
  protected final void fillParameters(List<String> commandLine) {
    commandLine.add("-b:v");
    commandLine.add(bitrate + "k");
    commandLine.add("-crf");
    commandLine.add("40");
    commandLine.add("-vf");
    commandLine.add("\"scale=iw/2:ih/2\"");
  }
}
