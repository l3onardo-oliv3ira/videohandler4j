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

import static com.github.videohandler4j.imp.TimeTools.parse;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

import com.github.filehandler4j.imp.FileWrapper;
import com.github.utils4j.IConstants;
import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.Environment;
import com.github.utils4j.imp.Streams;
import com.github.utils4j.imp.function.Caller;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.imp.exception.FFMpegNotFoundException;
import com.github.videohandler4j.imp.exception.VideoDurationNotFound;

public enum VideoTools implements Caller<File, IVideoFile, VideoDurationNotFound> {
  FFMPEG("ffmpeg.exe");
  
  private final String fileName;
  
  private VideoTools(String fileName) {
    this.fileName = fileName;
  }
  
  public final Optional<Path> fullPath() {
    return Environment.resolveTo("FFMPEG_HOME", fileName, true, true);
  }
  
  @Override
  public IVideoFile call(File file) throws VideoDurationNotFound {
    Args.requireNonNull(file, "input is null");
    try {
      Path ffmpeg = fullPath().orElseThrow(FFMpegNotFoundException::new);
      final Process process = new ProcessBuilder(
        ffmpeg.toFile().getCanonicalPath(),
        "-i",
        file.getAbsolutePath(),
        "-hide_banner"
      ).redirectErrorStream(true).start();
      
      String output;
      try(InputStream input = process.getInputStream()) {
        output = Streams.readOutStream(input, IConstants.CP_850).get();
        process.waitFor();
      } finally {
        process.destroyForcibly();
      }
      
      final String durationPrefix = "Duration: ";
      int idx = output.indexOf(durationPrefix);
      if (idx < 0)
        throw new VideoDurationNotFound();
      
      final int length = output.length();
      final int start = idx += durationPrefix.length();
      char chr;
      while(idx < length && (Character.isDigit(chr = output.charAt(idx)) || chr == ':'))
        idx++;

      String durationText = output.substring(start, idx);
      Duration duration = parse(durationText).orElseThrow(VideoDurationNotFound::new);
      return new VideoFile(file, duration);
    } catch (VideoDurationNotFound e) {
      throw e;
    } catch (Exception e) {
      throw new VideoDurationNotFound(e);
    }
  }
  
  private static class VideoFile extends FileWrapper implements IVideoFile {

    private final Duration duration;
    
    private VideoFile(File file, Duration duration) {
      super(file);
      this.duration = duration;
    }
    
    @Override
    public Duration getDuration() {
      return duration;
    }

    @Override
    public long getDuration(TemporalUnit unit) {
      return duration.get(unit);
    }
  }
}
