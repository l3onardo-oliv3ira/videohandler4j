package com.github.videohandler4j.imp.event;

import java.io.File;

import com.github.filehandler4j.imp.event.FileOutputEvent;
import com.github.videohandler4j.IVideoOutputEvent;

public class VideoOutputEvent extends FileOutputEvent implements IVideoOutputEvent {

  private final long totalTime;

  public VideoOutputEvent(String message, File output, long totalTime) {
    super(message, output);
    this.totalTime = totalTime;
  }
  
  @Override
  public long getTotalTime() {
    return totalTime;
  }

  @Override
  public final String toString() {
    return super.toString() + " totalPages: " + totalTime;
  }

}
