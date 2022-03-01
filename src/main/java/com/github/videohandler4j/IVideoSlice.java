package com.github.videohandler4j;

import com.github.filehandler4j.IFileSlice;

public interface IVideoSlice extends IFileSlice {
  long getTime();

  long getTime(IVideoFile file);
  
  long end(IVideoFile file);
  
  String outputFileName();
  
  String outputFileName(IVideoFile file);

}
