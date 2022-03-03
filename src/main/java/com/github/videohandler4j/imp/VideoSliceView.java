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
  public void cancelCode(Runnable cancelCode) {
    
  }

  @Override
  public void splitAndSave(File input, File folder) {
    panel.splitAndSave(input, folder);
  }
}
