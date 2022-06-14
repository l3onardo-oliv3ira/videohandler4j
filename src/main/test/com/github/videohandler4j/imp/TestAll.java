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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import com.github.filehandler4j.IFileHandler;
import com.github.videohandler4j.IVideoFile;

public class TestAll {
  
  public static void main(String[] args) throws Exception {
    
    final String[] outputPath = new String[] {
      "byslice",
      "byduration",
      "bysize",
      "byaudio",
      "bywebm"
    };
    
    Path baseInput = Paths.get("E:\\jfms\\test-shell\\videos\\audiencia\\");

    for(int i = 0; i < outputPath.length; i++) {
      IVideoFile file = VideoTools.FFMPEG.call(baseInput.resolve("fim.mp4").toFile());
      VideoDescriptor desc = new VideoDescriptor.Builder(i == 3 ? ".ogg" : (i == 4 ? ".webm" : ".mp4"))
        .add(file)
        .output(baseInput.resolve(outputPath[i]))
        .build();
      IFileHandler<?> handler = null;
      switch(i) {
      case 0:
        final Duration _2minutes = Duration.ofMinutes(2);
        final Duration _3minutes = Duration.ofMinutes(3);
        final Duration _4minutes = Duration.ofMinutes(4);
        
        long sliceStartMillis = 0;
        handler = new BySliceVideoSplitter(
          new DefaultVideoSlice(sliceStartMillis, sliceStartMillis += _2minutes.toMillis()),
          new DefaultVideoSlice(sliceStartMillis, sliceStartMillis += _3minutes.toMillis()),
          new DefaultVideoSlice(sliceStartMillis, sliceStartMillis += _4minutes.toMillis()),
          new DefaultVideoSlice(sliceStartMillis, file.getDuration().toMillis())
        );
        break;
      case 1:
        final Duration maxSliceDuration = Duration.ofMinutes(10); //00:10:00
        handler = new ByDurationVideoSplitter(file, maxSliceDuration);
        break;
      case 2:
        final long maxSliceSize = 20 * 1024 * 1024; //20MB
        handler = new BySizeVideoSplitter(file, maxSliceSize);
        break;
      case 3:
        handler = new OggAudioExtractor();
        break;
      case 4:
        handler = new WebmConverter();
        break;
      }
      if (handler != null) {
        handler.apply(desc).subscribe((s) -> {
          System.out.println(s.toString());
        });
      }
    }
  }
}
