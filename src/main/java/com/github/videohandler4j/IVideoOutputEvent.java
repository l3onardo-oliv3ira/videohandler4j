package com.github.videohandler4j;

import com.github.filehandler4j.IFileOutputEvent;

public interface IVideoOutputEvent extends IFileOutputEvent, IVideoInfoEvent {
  long getTotalTime();
}
