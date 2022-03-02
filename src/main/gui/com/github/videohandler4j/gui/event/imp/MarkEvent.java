package com.github.videohandler4j.gui.event.imp;

import com.github.videohandler4j.gui.IMarkEvent;

public abstract class MarkEvent implements IMarkEvent {

  private long time;
  
  public MarkEvent(long time) {
    this.time = time;
  }
  
  @Override
  public final long getTime(){
    return time;
  }
}
