
package com.github.videohandler4j.gui.imp;

import static com.github.utils4j.imp.Strings.latinise;
import static com.github.utils4j.imp.Strings.text;
import static com.github.utils4j.imp.SwingTools.invokeLater;
import static com.github.utils4j.imp.Threads.startAsync;
import static com.github.videohandler4j.imp.VideoTool.FFMPEG;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import com.github.utils4j.gui.imp.AbstractPanel;
import com.github.utils4j.gui.imp.DefaultFileChooser;
import com.github.utils4j.imp.Strings;
import com.github.utils4j.imp.Threads;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;
import com.github.videohandler4j.imp.BySliceVideoSplitter;
import com.github.videohandler4j.imp.VideoDescriptor;
import com.github.videohandler4j.imp.VideoSlice;

import net.miginfocom.swing.MigLayout;

public class CutPanel extends AbstractPanel  {
  
  private static final Consumer<CutPanel> NOTHING = (slice) -> {};
  
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

  private Consumer<CutPanel> onClosed = NOTHING;
  
  private Consumer<CutPanel> onPlay = NOTHING;
  
  private Consumer<CutPanel> onStoped = NOTHING;
  
  private Consumer<CutPanel> onSaved = NOTHING;
  
  private Consumer<CutPanel> onSelected = NOTHING;
  
  private Consumer<CutPanel> onDoSelect = NOTHING;
  
  private IVideoSlice slice;

  public CutPanel(long start, long end) {
    super("/vh4j/icons/buttons/");
    
    playPauseButton.setIcon(playIcon);
    playPauseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDoSelect.accept(CutPanel.this);
        onPlay.accept(CutPanel.this);
      }
    });
    
    stopButton.setIcon(stopIcon);
    stopButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDoSelect.accept(CutPanel.this);
        onStoped.accept(CutPanel.this);
      }
    });
    
    saveButton.setIcon(saveIcon);
    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDoSelect.accept(CutPanel.this);
        onSaved.accept(CutPanel.this);
      }
    });
    
    closeButton.setIcon(closeIcon);
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onDoSelect.accept(CutPanel.this);
        onClosed.accept(CutPanel.this);
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
        onSelected.accept(CutPanel.this);
      }
    };
    
    MouseListener s2 = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        onDoSelect.accept(CutPanel.this);
      }
    };

    txtFragName.addMouseListener(s2);
    addMouseListener(s1);
    setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    refresh(new VideoSlice(start, end));
  }
  
  private void refresh(IVideoSlice slice) {
    this.slice = slice;
    this.startTime.setText(slice.startString());
    this.endTime.setText(slice.endString());
    this.lengthTime.setText(slice.timeString());
  }

  public final long start() {
    return this.slice.start();
  }
  
  public final long end() {
    return this.slice.end();
  }

  public final void setStart(long start) {
    refresh(new VideoSlice(start, this.slice.end()));
  }
  
  public final void setEnd(long end) {
    refresh(new VideoSlice(this.slice.start(), end));
  }
  
  public final IVideoSlice slice() {
    return slice;
  }
  
  public CutPanel setOnClosed(Consumer<CutPanel> onClosed) {
    if (onClosed != null) {
      this.onClosed = onClosed;
    }
    return this;
  }

  public CutPanel setOnPlay(Consumer<CutPanel> onPlay) {
    if (onPlay != null) {
      this.onPlay = onPlay;
    }
    return this;
  }

  public CutPanel setOnStop(Consumer<CutPanel> onStop) {
    if (onStop != null) {
      this.onStoped = onStop;
    }
    return this;
  }

  public CutPanel setOnSave(Consumer<CutPanel> onSave) {
    if (onSave != null) {
      this.onSaved = onSave;
    }
    return this;
  }
  
  public CutPanel setOnSelected(Consumer<CutPanel> onSelect) {
    if (onSelect != null) {
      this.onSelected = onSelect;
    }
    return this;
  }

  public CutPanel setOnDoSelect(Consumer<CutPanel> onDoSelect) {
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

  public void save(File currentMedia) {
    final JFileChooser chooser = new DefaultFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setDialogTitle("Selecione onde será gravado o vídeo");
    if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(null)) {
      return;
    }
    final File outputFolder = chooser.getSelectedFile();
    startAsync("fatia: de " + slice.startString() + " ate " + slice.endString(), () -> {
      showProgress("Processando divisão...");
      IVideoFile file = FFMPEG.call(currentMedia);
      VideoDescriptor desc;
      try {
        String namePrefix = latinise(text(txtFragName.getText()), false, false, 90);
        namePrefix = namePrefix.replaceAll("[\\\\/:*?\"<>|]", "");
        if (!namePrefix.isEmpty())
          namePrefix += '_';
        desc = new VideoDescriptor.Builder(".mp4")
          .namePrefix(namePrefix)
          .add(file)
          .output(outputFolder.toPath())
          .build();
      } catch (IOException e1) {
        return;
      }
      new BySliceVideoSplitter(slice).apply(desc).subscribe();
      hideProgress();
    });
  }
}
