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



package com.github.videohandler4j.gui.imp;

import static com.github.utils4j.gui.imp.SwingTools.invokeLater;
import static com.github.utils4j.imp.Strings.empty;
import static com.github.utils4j.imp.Strings.trim;
import static com.github.utils4j.imp.Threads.startAsync;
import static com.github.videohandler4j.imp.VideoTools.FFMPEG;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.github.progress4j.IProgressView;
import com.github.progress4j.imp.ProgressFactory;
import com.github.progress4j.imp.Stage;
import com.github.utils4j.gui.imp.AbstractPanel;
import com.github.utils4j.gui.imp.ExceptionAlert;
import com.github.utils4j.imp.Args;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;
import com.github.videohandler4j.imp.BySliceVideoSplitter;
import com.github.videohandler4j.imp.VideoDescriptor;

import net.miginfocom.swing.MigLayout;

public class VideoSlicePanel extends AbstractPanel  { 
  
  private static final Consumer<JPanel> NOTHING = (slice) -> {};
  
  private final Icon playIcon = newIcon("play");

  private final Icon stopIcon = newIcon("stop");
  
  private final Icon saveIcon = newIcon("save");
  
  private final Icon closeIcon = newIcon("close");
  
  private final Icon cancelIcon = newIcon("cancel");
  
  private final JTextField txtFragName = new JTextField();
  
  private final JLabel startTime = new JLabel("--:--:--");

  private final JLabel endTime = new JLabel("--:--:--");

  private final JButton playPauseButton = new StandardButton();

  private final JButton stopButton = new StandardButton();
  
  private final JLabel lengthTime = new JLabel("00:00:00");

  private final JButton closeButton = new StandardButton();
  
  private final JButton saveButton = new StandardButton();
  
  private final JButton cancelButton = new StandardButton();
  
  private final JProgressBar progress = new JProgressBar();

  private Consumer<JPanel> onClosed = NOTHING;
  
  private Consumer<JPanel> onPlay = NOTHING;
  
  private Consumer<JPanel> onStoped = NOTHING;
  
  private Consumer<JPanel> onSaved = NOTHING;
  
  private Consumer<JPanel> onSelected = NOTHING;
  
  private Consumer<JPanel> onDoSelect = NOTHING;
  
  private IVideoSlice slice;

  public VideoSlicePanel(IVideoSlice slice) {
    super("/vh4j/icons/buttons/");
    
    playPauseButton.setIcon(playIcon);
    playPauseButton.addActionListener((e) -> {
      onDoSelect.accept(VideoSlicePanel.this);
      onPlay.accept(VideoSlicePanel.this);
    });
    
    stopButton.setIcon(stopIcon);
    stopButton.addActionListener((e) -> {
      onDoSelect.accept(VideoSlicePanel.this);
      onStoped.accept(VideoSlicePanel.this);
    });
    
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener((e) -> {
      onDoSelect.accept(VideoSlicePanel.this);
      onSaved.accept(VideoSlicePanel.this);
    });
    
    closeButton.setIcon(closeIcon);
    closeButton.addActionListener((e) -> {
      onClosed.accept(VideoSlicePanel.this);
    });
    
    cancelButton.setIcon(cancelIcon);
    cancelButton.setVisible(true);
    cancelButton.addActionListener((e) -> {
      if (async != null) {
        async.interrupt();
      }
    });
    
    progress.setVisible(true);

    setLayout(new MigLayout());
    add(txtFragName, "span, pushx, growx, wrap");
    add(startTime);
    add(endTime, "al right, wrap");
    add(playPauseButton, "split 4");
    add(stopButton);
    add(saveButton);
    add(lengthTime);
    add(closeButton, "al right, wrap");
    
//    add(progress, "growx");
//    add(cancelButton, "al right");
    
    MouseListener s1 = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        onSelected.accept(VideoSlicePanel.this);
      }
    };
    
    MouseListener s2 = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        onSelected.accept(VideoSlicePanel.this);
      }
    };

    txtFragName.setMaximumSize(new Dimension(190, txtFragName.getPreferredSize().height));
    txtFragName.setSize(new Dimension(190, txtFragName.getPreferredSize().height));
    txtFragName.addMouseListener(s2);
    addMouseListener(s1);
    setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    refresh(slice);
  }
  
  public final IVideoSlice refresh(IVideoSlice slice) {
    this.slice = slice;
    this.startTime.setText(slice.startString());
    this.endTime.setText(slice.endString());
    this.lengthTime.setText(slice.timeString());
    this.saveButton.setEnabled(slice.end() < Long.MAX_VALUE);
    return this.slice;
  }

  public final VideoSlicePanel setOnClosed(Consumer<JPanel> onClosed) {
    if (onClosed != null) {
      this.onClosed = onClosed;
    }
    return this;
  }

  public final VideoSlicePanel setOnPlay(Consumer<JPanel> onPlay) {
    if (onPlay != null) {
      this.onPlay = onPlay;
    }
    return this;
  }

  public final VideoSlicePanel setOnStop(Consumer<JPanel> onStop) {
    if (onStop != null) {
      this.onStoped = onStop;
    }
    return this;
  }

  public final VideoSlicePanel setOnSave(Consumer<JPanel> onSave) {
    if (onSave != null) {
      this.onSaved = onSave;
    }
    return this;
  }
  
  public final VideoSlicePanel setOnSelected(Consumer<JPanel> onSelect) {
    if (onSelect != null) {
      this.onSelected = onSelect;
    }
    return this;
  }

  public final VideoSlicePanel setOnDoSelect(Consumer<JPanel> onDoSelect) {
    if (onDoSelect != null) {
      this.onDoSelect = onDoSelect;
    }
    return this;
  }

  private void showProgress(String text) {
    invokeLater(() -> {
      this.progress.setIndeterminate(true);
      this.progress.setStringPainted(true);
      this.progress.setString(text);
      this.progress.setVisible(true);
      this.cancelButton.setVisible(true);
      VideoSlicePanel.this.add(progress, "span, pushx, growx");
      VideoSlicePanel.this.updateUI();
    });
  }
  
  private void hideProgress() {
    invokeLater(() -> {
      this.progress.setVisible(false);
      this.cancelButton.setVisible(false);
      VideoSlicePanel.this.remove(progress);
      VideoSlicePanel.this.updateUI();
    });
  }

  private volatile Thread async;
  private static ProgressFactory PROGRESS_FACTORY = new ProgressFactory();
  
  public void splitAndSave(File outputFile, File outputFolder) {
    if (!this.saveButton.isEnabled()) 
      return;
    Args.requireExists(outputFile, "outputFile does not exists");
    Args.requireNonNull(outputFolder, "outputFolder does not exists");
    final String sliceString = "Corte: de " + slice.startString() + " ate " + slice.endString();
    async = startAsync(sliceString, () -> {
      
      IProgressView progress = PROGRESS_FACTORY.get();
      try {
        progress.display();
        progress.begin(new Stage(sliceString));
        showProgress("Processando divisão...");
        
        IVideoFile file = FFMPEG.call(outputFile);
        String namePrefix = trim(txtFragName.getText()).replaceAll("[\\\\/:*?\"<>|]", empty());
        if (!namePrefix.isEmpty())
          namePrefix += '_';
       
        new BySliceVideoSplitter(false, slice).apply(
          new VideoDescriptor.Builder(".mp4")
            .namePrefix(namePrefix)
            .add(file)
            .output(outputFolder.toPath())
            .build()
          )
        .subscribe(
          e -> progress.info(e.getMessage()),
          e -> progress.abort(e)
        );
        
        if (progress.getAbortCause() != null) {
          throw progress.getAbortCause();
        }
        progress.end();
        
      } catch (InterruptedException e) {
        ExceptionAlert.show("Operação cancelada!", "Arquivo: " + outputFile.getAbsolutePath(), e);
      } catch (Throwable e) {
        ExceptionAlert.show("Não foi possível dividir o vídeo", "Arquivo: " + outputFile.getAbsolutePath(), e);
      } finally {
        async = null;
        hideProgress();
        progress.undisplay();
        progress.dispose();
      }
    });
  }
}
