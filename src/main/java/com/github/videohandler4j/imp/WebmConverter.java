package com.github.videohandler4j.imp;

import java.util.List;

public class WebmConverter extends FFMPEGHandler {

  @Override
  protected final void fillParameters(List<String> commandLine) {
    commandLine.add("-map");
    commandLine.add("0:v:0");
    commandLine.add("-map");
    commandLine.add("0:a:0");
    commandLine.add("-c:v");
    commandLine.add("libvpx");
    commandLine.add("-b:v");
    commandLine.add("512k");
    commandLine.add("-crf");
    commandLine.add("40");
    commandLine.add("-max_muxing_queue_size");
    commandLine.add("89478485");
    commandLine.add("-c:a");
    commandLine.add("libvorbis");
    commandLine.add("-ac");
    commandLine.add("2");
    commandLine.add("-strict");
    commandLine.add("experimental");
    commandLine.add("-vf");
    commandLine.add("scale='-1:min(ih,320)'");
  }
}
