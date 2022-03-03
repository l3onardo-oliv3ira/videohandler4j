
package com.github.videohandler4j.gui.imp;

import static com.github.utils4j.imp.Strings.defaultLatin;
import static com.github.utils4j.imp.Strings.text;
import static com.github.utils4j.imp.SwingTools.invokeLater;
import static com.github.utils4j.imp.Threads.startAsync;
import static com.github.videohandler4j.imp.VideoTool.FFMPEG;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.github.utils4j.gui.imp.AbstractPanel;
import com.github.utils4j.gui.imp.FixedLengthDocument;
import com.github.utils4j.imp.Args;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;
import com.github.videohandler4j.imp.BySliceVideoSplitter;
import com.github.videohandler4j.imp.VideoDescriptor;

import net.miginfocom.swing.MigLayout;

public class VideoSlacePanel extends AbstractPanel  { 
  
  private static final Consumer<JPanel> NOTHING = (slice) -> {};
  
  private final Icon playIcon = newIcon("play");

  private final Icon stopIcon = newIcon("stop");
  
  private final Icon saveIcon = newIcon("save");
  
  private final Icon closeIcon = newIcon("close");
  
  private final JTextField txtFragName = new JTextField();
  
  private final JLabel startTime = new JLabel("--:--:--");

  private final JLabel endTime = new JLabel("--:--:--");

  private final JButton playPauseButton = new StandardButton();

  private final JButton stopButton = new StandardButton();
  
  private final JLabel lengthTime = new JLabel("00:00:00");

  private final JButton closeButton = new StandardButton();
  
  private final JButton saveButton = new StandardButton();
  
  private final JProgressBar progress = new JProgressBar();

  private Consumer<JPanel> onClosed = NOTHING;
  
  private Consumer<JPanel> onPlay = NOTHING;
  
  private Consumer<JPanel> onStoped = NOTHING;
  
  private Consumer<JPanel> onSaved = NOTHING;
  
  private Consumer<JPanel> onSelected = NOTHING;
  
  private Consumer<JPanel> onDoSelect = NOTHING;
  
  private IVideoSlice slice;

  public VideoSlacePanel(IVideoSlice slice) {
    super("/vh4j/icons/buttons/");
    
    playPauseButton.setIcon(playIcon);
    playPauseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDoSelect.accept(VideoSlacePanel.this);
        onPlay.accept(VideoSlacePanel.this);
      }
    });
    
    stopButton.setIcon(stopIcon);
    stopButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDoSelect.accept(VideoSlacePanel.this);
        onStoped.accept(VideoSlacePanel.this);
      }
    });
    
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDoSelect.accept(VideoSlacePanel.this);
        onSaved.accept(VideoSlacePanel.this);
      }
    });
    
    closeButton.setIcon(closeIcon);
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onClosed.accept(VideoSlacePanel.this);
      }
    });
    
    progress.setVisible(false);

    setLayout(new MigLayout());
    add(txtFragName, "span, pushx, growx, wrap");
    add(startTime);
    add(endTime, "al right, wrap");
    add(playPauseButton, "split 4");
    add(stopButton);
    add(saveButton);
    add(lengthTime);
    add(closeButton, "al right, wrap");
    add(progress, "span, pushx, growx");
    
    MouseListener s1 = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        onSelected.accept(VideoSlacePanel.this);
      }
    };
    
    MouseListener s2 = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        onDoSelect.accept(VideoSlacePanel.this);
      }
    };

    txtFragName.setMaximumSize(new Dimension(190, txtFragName.getPreferredSize().height));
    txtFragName.setSize(new Dimension(190, txtFragName.getPreferredSize().height));
    txtFragName.addMouseListener(s2);
    addMouseListener(s1);
    setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    refresh(slice);
  }
  
  public IVideoSlice refresh(IVideoSlice slice) {
    this.slice = slice;
    this.startTime.setText(slice.startString());
    this.endTime.setText(slice.endString());
    this.lengthTime.setText(slice.timeString());
    return this.slice;
  }

  public final VideoSlacePanel setOnClosed(Consumer<JPanel> onClosed) {
    if (onClosed != null) {
      this.onClosed = onClosed;
    }
    return this;
  }

  public final VideoSlacePanel setOnPlay(Consumer<JPanel> onPlay) {
    if (onPlay != null) {
      this.onPlay = onPlay;
    }
    return this;
  }

  public final VideoSlacePanel setOnStop(Consumer<JPanel> onStop) {
    if (onStop != null) {
      this.onStoped = onStop;
    }
    return this;
  }

  public final VideoSlacePanel setOnSave(Consumer<JPanel> onSave) {
    if (onSave != null) {
      this.onSaved = onSave;
    }
    return this;
  }
  
  public final VideoSlacePanel setOnSelected(Consumer<JPanel> onSelect) {
    if (onSelect != null) {
      this.onSelected = onSelect;
    }
    return this;
  }

  public final VideoSlacePanel setOnDoSelect(Consumer<JPanel> onDoSelect) {
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
    });
  }
  
  private void hideProgress() {
    invokeLater(() -> {
      this.progress.setVisible(false);
    });
  }

  public void splitAndSave(File outputFile, File outputFolder) {
    Args.requireExists(outputFile, "outputFile does not exists");
    Args.requireNonNull(outputFolder, "outputFolder does not exists");
    startAsync("fatia: de " + slice.startString() + " ate " + slice.endString(), () -> {
      try {
        showProgress("Processando divisão...");
        IVideoFile file = FFMPEG.call(outputFile);
        String namePrefix = defaultLatin(text(txtFragName.getText()), 90)
            .replaceAll("[\\\\/:*?\"<>|]", "");
        if (!namePrefix.isEmpty())
          namePrefix += '_';
        new BySliceVideoSplitter(slice).apply(
          new VideoDescriptor.Builder(".mp4")
            .namePrefix(namePrefix)
            .add(file)
            .output(outputFolder.toPath())
            .build()
          ).subscribe();
      } catch (Exception e1) {
        return;
      } finally {
        hideProgress();
      }
    });
  }
}
