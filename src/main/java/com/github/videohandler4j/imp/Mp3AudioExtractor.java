package com.github.videohandler4j.imp;

import java.util.List;

public class Mp3AudioExtractor extends FFMPEGHandler {

  @Override
  protected final void fillParameters(List<String> commandLine) {
    commandLine.add("-q:a");
    commandLine.add("0");
    commandLine.add("-map");
    commandLine.add("a");
  }
}


