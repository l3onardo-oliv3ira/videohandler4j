package com.github.videohandler4j.imp;

import java.io.File;

import com.github.filehandler4j.imp.AbstractFileRageHandler;
import com.github.utils4j.imp.NotImplementedException;
import com.github.videohandler4j.IVideoInfoEvent;
import com.github.videohandler4j.imp.event.VideoInfoEvent;

import io.reactivex.Emitter;

abstract class AbstractVideoSplitter extends AbstractFileRageHandler<IVideoInfoEvent> {

  protected long timeNumber = 0;
  private File currentOutput = null;

  public AbstractVideoSplitter() {
    this(new VideoSlice());
  }
  
  public AbstractVideoSplitter(VideoSlice... ranges) {
    super(ranges);
    this.reset();
  }
  
  @Override
  protected void handleError(Throwable e) {    
    this.clearOutput();
    super.handleError(e);
  }

  private void clearOutput() {
    if (currentOutput != null) {
      currentOutput.delete();
      currentOutput = null;
    }
  }

  @Override
  public void reset() {
    timeNumber = 0;
    currentOutput = null;
    super.reset();
  }  
  
  @Override
  protected void handle(File file, Emitter<IVideoInfoEvent> emitter) throws Exception {
    emitter.onNext(new VideoInfoEvent("Processando arquivo " + file.getName()));
    throw new NotImplementedException();  //WE HAVE TO GO BACK HERE!
  }  
}
