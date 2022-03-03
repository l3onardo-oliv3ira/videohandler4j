package com.github.videohandler4j;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JPanel;

import com.github.utils4j.ICanceller;

public interface IVideoSliceView extends IVideoSlice, ICanceller {
  IVideoSliceView setOnDoSelect(Consumer<IVideoSliceView> onDoSelect);
  IVideoSliceView setOnClosed(Consumer<IVideoSliceView> onClosed);
  IVideoSliceView setOnPlay(Consumer<IVideoSliceView> onPlay);
  IVideoSliceView setOnStop(Consumer<IVideoSliceView> onStop);
  IVideoSliceView setOnSave(Consumer<IVideoSliceView> onSave);
  IVideoSliceView setOnSelected(Consumer<IVideoSliceView> onSelect);

  JPanel asPanel();
  
  void setEnd(long time);
  void splitAndSave(File input, File folder);
}