package com.github.videohandler4j;

import com.github.filehandler4j.IFileRange;

public interface IVideoSlice extends IFileRange {
  long getTime();

  long getTime(IVideoFile file);
  
  long end(IVideoFile file);
  
  String outputFileName();
  
  String outputFileName(IVideoFile file);

}
