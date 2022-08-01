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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.github.utils4j.IHasDuration;
import com.github.utils4j.imp.Args;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;

public class TimeTools {
  
  private TimeTools() {}

  public static IVideoSlice[] slices(IVideoFile file, long maxSliceFileSize) {
    return slices(file, maxSliceFileSize, 0);
  }
  
  public static IVideoSlice[] slices(IVideoFile file, long maxSliceFileSize, long sliceStart) {
    return slices(file, maxSliceFileSize, sliceStart, 0);
  }
  
  public static IVideoSlice[] slices(IVideoFile file, long maxSliceFileSize, long sliceStart, long previousMarging) {
    Args.requireNonNull(file, "file is null");
    Args.requireZeroPositive(maxSliceFileSize, "maxSize < 0");
    Args.requireZeroPositive(sliceStart, "sliceStart < 0");
    long fileDurationMillis = file.getDuration().toMillis();
    long fileSize = file.length();
    long sizePerDuration = fileSize / fileDurationMillis;
    long sliceDurationMillis = maxSliceFileSize / sizePerDuration;
    return slices(file, Duration.ofMillis(sliceDurationMillis), sliceStart, previousMarging);
  }
  
  public static IVideoSlice[] slices(IHasDuration file, Duration maxDurationSlice) {
    return slices(file, maxDurationSlice, 0);
  }

  public static IVideoSlice[] slices(IHasDuration file, Duration maxDurationSlice, long sliceStart) {
    return slices(file, maxDurationSlice, sliceStart, 0);
  }
  
  public static IVideoSlice[] slices(IHasDuration file, Duration maxDurationSlice, long sliceStart, long previousMarging) {
    Args.requireNonNull(file, "file is null");
    Args.requireNonNull(maxDurationSlice, "maxDurationSlice is null");
    Args.requireZeroPositive(sliceStart, "sliceStart < 0");
    List<IVideoSlice> slices = new ArrayList<>();
    long durationVideoMillis = file.getDuration().toMillis();
    long durationSliceMillis = maxDurationSlice.toMillis();
    for(long start = sliceStart; start < durationVideoMillis; start += durationSliceMillis) {
      slices.add(new DefaultVideoSlice(Math.max(0, start - previousMarging), Math.min(durationVideoMillis, start + durationSliceMillis)));
    }
    return slices.toArray(new IVideoSlice[slices.size()]);
  }
}

