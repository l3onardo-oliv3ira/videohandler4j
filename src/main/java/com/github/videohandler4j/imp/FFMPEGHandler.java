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

import static com.github.utils4j.imp.Directory.stringPath;
import static com.github.videohandler4j.imp.VideoTools.FFMPEG;

import java.io.File;
import java.util.List;

import com.github.filehandler4j.IInputFile;
import com.github.filehandler4j.imp.AbstractFileHandler;
import com.github.utils4j.imp.Containers;
import com.github.videohandler4j.IVideoInfoEvent;
import com.github.videohandler4j.imp.exception.FFMpegNotFoundException;

import io.reactivex.Emitter;

public abstract class FFMPEGHandler extends AbstractFileHandler<IVideoInfoEvent> {

  @Override
  protected final void handle(IInputFile file, Emitter<IVideoInfoEvent> emitter) throws Exception {
    
    final File ffmpegHome = FFMPEG.fullPath().orElseThrow(FFMpegNotFoundException::new).toFile();
    final File outputVideo = resolveOutput(file.getShortName() + ".mp4");
    outputVideo.delete();

    List<String> commandLine = Containers.arrayList(
      stringPath(ffmpegHome),
      "-y",
      "-i",
      file.getAbsolutePath(),
      "-stats_period",
      "1.5",
      "-hide_banner",
      "-nostdin"
    );
    fillParameters(commandLine);
    
    new FFMPEGProcessor(commandLine, outputVideo).proccess(file, emitter);
  }
  
  protected abstract void fillParameters(List<String> parameters);
}
