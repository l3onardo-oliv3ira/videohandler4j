package com.github.videohandler4j.imp.exception;

public class VideoDurationNotFound extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public VideoDurationNotFound() {
    super("Could not compute video duration");
  }

  public VideoDurationNotFound(Exception cause) {
    super(cause);
  }
}
