package com.github.videohandler4j.imp.event;

import com.github.filehandler4j.imp.event.FileInfoEvent;
import com.github.videohandler4j.IVideoInfoEvent;

public class VideoInfoEvent extends FileInfoEvent implements IVideoInfoEvent{
  public VideoInfoEvent(String message) {
    super(message);
  }
}
