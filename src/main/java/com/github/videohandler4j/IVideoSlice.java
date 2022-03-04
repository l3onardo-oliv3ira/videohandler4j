package com.github.videohandler4j;

import com.github.filehandler4j.IFileSlice;
import com.github.utils4j.IDurationProvider;

public interface IVideoSlice extends IFileSlice {

  long getTime();

  long getTime(IDurationProvider file);
  
  long end(IDurationProvider file);
  
  String startString();
  
  String endString();

  String timeString();

  String outputFileName();
  
  String outputFileName(IDurationProvider file);

}
