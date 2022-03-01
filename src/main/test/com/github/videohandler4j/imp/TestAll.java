package com.github.videohandler4j.imp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.github.filehandler4j.IFileHandler;
import com.github.videohandler4j.IVideoFile;

public class TestAll {
  
  public static void main(String[] args) throws IOException {
    
    final String[] outputPath = new String[] {
      "byslice",
      "byduration",
      "bysize"
    };
    
    Path baseInput = Paths.get("D:/temp/");

    for(int i = 0; i < outputPath.length; i++) {
      IVideoFile file = VideoTool.FFMPEG.call(baseInput.resolve("video.mp4").toFile());
      VideoDescriptor desc = new VideoDescriptor.Builder(".mp4")
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
          new VideoSlice(sliceStartMillis, sliceStartMillis += _2minutes.toMillis()),
          new VideoSlice(sliceStartMillis, sliceStartMillis += _3minutes.toMillis()),
          new VideoSlice(sliceStartMillis, sliceStartMillis += _4minutes.toMillis()),
          new VideoSlice(sliceStartMillis, file.getDuration(ChronoUnit.MILLIS))
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
      }
      if (handler != null) {
        handler.apply(desc).subscribe((s) -> {
          System.out.println(s.toString());
        });
      }
    }
  }
}
