package com.github.videohandler4j.imp.event;

import com.github.filehandler4j.imp.event.FileInfoEvent;
import com.github.videohandler4j.IVideoSliceEvent;
import com.github.videohandler4j.imp.TimeTools;

public class VideoSliceEvent extends FileInfoEvent implements IVideoSliceEvent {

  private final long startTime;
  private final long totalTime;

  public VideoSliceEvent(String message, long startTime, long totalTime) {
    super(message);
    this.startTime = startTime;
    this.totalTime = totalTime;
  }
  
  @Override
  public final long getStartTime() {
    return startTime;
  }

  @Override
  public final long getTotalTime() {
    return totalTime;
  }

  @Override
  public final String toString() {
    return getMessage() + " startTime: " + startTime + " of " + totalTime;
  }

  @Override
  public final String getStartTimeString() {
    return TimeTools.toHmsString(startTime);
  }

  @Override
  public final String getTotalTimeString() {
    return TimeTools.toHmsString(totalTime);
  }

}
