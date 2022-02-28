package com.github.videohandler4j;

public interface IVideoSliceEvent extends IVideoInfoEvent {
  long getStartTime();
  
  long getTotalTime();

  String getStartTimeString();
  
  String getTotalTimeString();
}
