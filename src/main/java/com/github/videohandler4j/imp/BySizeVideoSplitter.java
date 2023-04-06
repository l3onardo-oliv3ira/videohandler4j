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

import static com.github.videohandler4j.imp.TimeTools.slices;

import java.io.File;

import com.github.utils4j.imp.ArrayIterator;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;

public class BySizeVideoSplitter extends AbstractVideoSplitter{
  
  private final long maxSliceFileSize;
  
  private final IVideoFile file;
  
  private float percent = 0.95f; //5 percent discount
  
  public BySizeVideoSplitter(IVideoFile file, long maxSliceFileSize) {
    this(file, maxSliceFileSize, true);
  }
  
  public BySizeVideoSplitter(IVideoFile file, long maxSliceFileSize, boolean partPrefix) {
    super(partPrefix, slices(file, maxSliceFileSize, 0, DEFAULT_PREVIOUS_MARGING));
    this.maxSliceFileSize = maxSliceFileSize;
    this.file = file;
  }
  
  @Override
  protected boolean forceCopy(IVideoFile file) {
    return file.length() <= maxSliceFileSize;
  }
  
  @Override
  protected boolean accept(File sliceFile, IVideoSlice slice) {
    //Uma tolerância de 5 segundos de vídeo
    if (percent <= 0.05 || slice.getTime() <= 5000 ||  sliceFile.length() <= maxSliceFileSize) {
      return true;
    }
    long smallerSize = (long)(percent * maxSliceFileSize);
    percent -= 0.05; //5 percent discount
    setIterator(new ArrayIterator<IVideoSlice>(slices(file, smallerSize, slice.start(), DEFAULT_PREVIOUS_MARGING)));
    return false;
  } 
}
