package com.github.videohandler4j;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

import com.github.filehandler4j.IInputFile;

public interface IVideoFile extends IInputFile {

  Duration getDuration();
  
  long getDuration(TemporalUnit unit);
}
