package com.github.videohandler4j.imp.exception;

public class FFMpegNotFoundException extends Exception {
  private static final long serialVersionUID = 1L;

  public FFMpegNotFoundException() {
    super("Could not find ffmpeg.exe");
  }
}
