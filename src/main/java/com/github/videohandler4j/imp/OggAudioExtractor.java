package com.github.videohandler4j.imp;

import java.util.List;

public class OggAudioExtractor extends FFMPEGHandler {

  @Override
  protected final void fillParameters(List<String> commandLine) {
    commandLine.add("-acodec");
    commandLine.add("libvorbis");
    commandLine.add("-aq");
    commandLine.add("3");
    commandLine.add("-vn");
    commandLine.add("-ac");
    commandLine.add("2");
  }
}
