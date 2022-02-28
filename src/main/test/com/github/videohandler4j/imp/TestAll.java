package com.github.videohandler4j.imp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

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
        Duration _2minutes = Duration.ofMinutes(2);
        int step = 0;
        handler = new BySliceVideoSplitter(
          new VideoSlice(step, step += _2minutes.toMillis()),
          new VideoSlice(step, step += _2minutes.toMillis()),
          new VideoSlice(step, step += _2minutes.toMillis()),
          new VideoSlice(step, file.getDuration())
        );
        break;
      case 1:
        handler = new ByDurationVideoSplitter(file, Duration.ofMinutes(5));
        break;
      case 2:
        handler = new BySizeVideoSplitter(file, 5 * 1024 * 1024);
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
