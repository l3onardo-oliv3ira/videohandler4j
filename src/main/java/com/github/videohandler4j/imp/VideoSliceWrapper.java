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

import com.github.filehandler4j.imp.FileSliceWrapper;
import com.github.utils4j.IDurationProvider;
import com.github.videohandler4j.IVideoSlice;

public class VideoSliceWrapper extends FileSliceWrapper implements IVideoSlice {
  public VideoSliceWrapper(IVideoSlice slice) {
    super(slice);
  }

  protected final IVideoSlice slice() {
    return super.<IVideoSlice>getSlice();
  }
  
  @Override
  public long getTime() {
    return slice().getTime();
  }

  @Override
  public long getTime(IDurationProvider file) {
    return slice().getTime(file);
  }

  @Override
  public long end(IDurationProvider file) {
    return slice().end(file);
  }

  @Override
  public String startString() {
    return slice().startString();
  }

  @Override
  public String endString() {
    return slice().endString();
  }

  @Override
  public String timeString() {
    return slice().timeString();
  }

  @Override
  public String outputFileName() {
    return slice().outputFileName();
  }

  @Override
  public String outputFileName(IDurationProvider file) {
    return slice().outputFileName(file);
  }
}
