/*
* MIT License
* 
* Copyright (c) 2022 Leonardo de Lima Oliveira
* 
* https://github.com/l3onardo-oliv3ira
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/


package com.github.videohandler4j.imp;

import static com.github.videohandler4j.imp.TimeTools.toHmsString;

import com.github.filehandler4j.imp.DefaultFileSlice;
import com.github.utils4j.IDurationProvider;
import com.github.videohandler4j.IVideoSlice;

public final class DefaultVideoSlice extends DefaultFileSlice implements IVideoSlice {

  public DefaultVideoSlice() {
    this(0);
  }

  public DefaultVideoSlice(long startTime) {
    this(startTime, Long.MAX_VALUE);
  }
  
  public DefaultVideoSlice(long startTime, long endTime) {
    super(startTime, endTime);
  }

  @Override
  public long getTime() {
    return super.end() - super.start();
  }
  
  @Override
  public String outputFileName() {
    return toHmsString(start()) + "_ate_" + toHmsString(end());
  }

  @Override
  public long getTime(IDurationProvider file) {
    return end(file) - super.start();
  }

  @Override
  public long end(IDurationProvider file) {
    return Math.min(super.end(), file.getDuration().toMillis());
  }
  
  @Override
  public String outputFileName(IDurationProvider file) {
    return toHmsString(start()) + "_ate_" + toHmsString(end(file));
  }

  @Override
  public String endString() {
    long end = super.end();
    return Long.MAX_VALUE == end ? "__:__:__" : TimeTools.toString(end);
  }

  @Override
  public String startString() {
    return TimeTools.toString(start());
  }

  @Override
  public String timeString() {
    long end = super.end();
    return end == Long.MAX_VALUE ? "__:__:__" : TimeTools.toString(getTime());
  }
}
