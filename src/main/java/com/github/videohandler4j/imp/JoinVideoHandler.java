package com.github.videohandler4j.imp;

import java.io.File;

import com.github.filehandler4j.IInputFile;
import com.github.filehandler4j.imp.AbstractFileHandler;
import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.StopWatch;
import com.github.videohandler4j.IVideoInfoEvent;
import com.github.videohandler4j.imp.event.VideoOutputEvent;

import io.reactivex.Emitter;

public class JoinVideoHandler extends AbstractFileHandler<IVideoInfoEvent> {

  private File output;
  private final String mergeFileName;
  private int totalTime;
  private StopWatch stopWatch = new StopWatch();
  
  public JoinVideoHandler(String mergeFileName) {
    this.mergeFileName = Args.requireText(mergeFileName, "mergeFileName is empty");
  }
  
  @Override
  public void reset() {
    this.output = null;
    this.totalTime = 0;
    this.stopWatch.reset();
    super.reset();
  }
  
  @Override
  protected void beforeHandle(Emitter<IVideoInfoEvent> emitter) throws Exception {
    totalTime = 0;
    stopWatch.reset();
    stopWatch.start();
  }

  @Override
  protected void handle(IInputFile file, Emitter<IVideoInfoEvent> emitter) throws Exception {
    StopWatch handleWatch = new StopWatch();
    totalTime += 1; 
    //WE HAVE TO GO BACK HERE
  }
  
  @Override
  protected void handleError(Throwable e) {    
    this.cleanOutput();
    super.handleError(e);
  }

  private void cleanOutput() {
    if (output != null) {
      output.delete();
      output = null;
    }
  }
  
  @Override
  protected void afterHandle(Emitter<IVideoInfoEvent> emitter) {
    long time = stopWatch.stop();
    emitter.onNext(new VideoOutputEvent("Gerado arquivo " + output.getName() + " em " + (time / 1000f) + " segundos ", output, totalTime));
  }  
}
