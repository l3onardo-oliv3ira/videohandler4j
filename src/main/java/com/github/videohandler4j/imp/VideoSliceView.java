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

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JPanel;

import com.github.videohandler4j.IVideoSliceView;
import com.github.videohandler4j.gui.imp.VideoSlicePanel;

public class VideoSliceView extends VideoSliceWrapper implements IVideoSliceView {

  private final VideoSlicePanel panel;
  
  public VideoSliceView() {
    this(0);
  }

  public VideoSliceView(long startTime) {
    this(startTime, Long.MAX_VALUE);
  }
  
  public VideoSliceView(long startTime, long endTime) {
    super(new DefaultVideoSlice(startTime, endTime));
    this.panel = new VideoSlicePanel(slice());
  }

  @Override
  public IVideoSliceView setOnClosed(Consumer<IVideoSliceView> onClosed) {
    panel.setOnClosed((c) -> onClosed.accept(this));
    return this;
  }

  @Override
  public IVideoSliceView setOnPlay(Consumer<IVideoSliceView> onPlay) {
    panel.setOnPlay((c) -> onPlay.accept(this));
    return this;
  }

  @Override
  public IVideoSliceView setOnStop(Consumer<IVideoSliceView> onStop) {
    panel.setOnStop((c) -> onStop.accept(this));
    return this;
  }

  @Override
  public IVideoSliceView setOnSave(Consumer<IVideoSliceView> onSave) {
    panel.setOnSave((c) -> onSave.accept(this));
    return this;
  }
  
  @Override
  public IVideoSliceView setOnSelected(Consumer<IVideoSliceView> onSelect) {
    panel.setOnSelected((c) -> onSelect.accept(this));
    return this;
  }

  @Override
  public IVideoSliceView setOnDoSelect(Consumer<IVideoSliceView> onDoSelect) {
    panel.setOnDoSelect((c) -> onDoSelect.accept(this));
    return this;
  }

  @Override
  public JPanel asPanel() {
    return panel;
  }
  
  @Override
  public void setEnd(long endTime) {
    super.setSlice(panel.refresh(new DefaultVideoSlice(start(), endTime)));
  }
  
  @Override
  public void splitAndSave(File input, File folder) {
    panel.splitAndSave(input, folder);
  }
}
