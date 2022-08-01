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

import java.util.List;

public class WebmConverter extends FFMPEGHandler {

  @Override
  protected final void fillParameters(List<String> commandLine) {
    commandLine.add("-map");
    commandLine.add("0:v:0");
    commandLine.add("-map");
    commandLine.add("0:a:0");
    commandLine.add("-c:v");
    commandLine.add("libvpx");
    commandLine.add("-b:v");
    commandLine.add("512k");
    commandLine.add("-crf");
    commandLine.add("40");
    commandLine.add("-max_muxing_queue_size");
    commandLine.add("89478485");
    commandLine.add("-c:a");
    commandLine.add("libvorbis");
    commandLine.add("-ac");
    commandLine.add("2");
    commandLine.add("-strict");
    commandLine.add("experimental");
    commandLine.add("-vf");
    commandLine.add("scale='-1:min(ih,320)'");
  }
}
