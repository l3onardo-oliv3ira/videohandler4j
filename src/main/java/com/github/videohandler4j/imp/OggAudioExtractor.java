package com.github.videohandler4j.imp;

import java.io.File;
import java.util.List;

import com.github.filehandler4j.IInputFile;
import com.github.utils4j.imp.Containers;

public class OggAudioExtractor extends FFMPEGHandler {

  @Override
  protected final List<String> getCommandLine(File ffmpegPath, IInputFile file) {
    return Containers.arrayList(
      ffmpegPath.getAbsolutePath(),
      "-y",
      "-i",
      file.getAbsolutePath(),
      "-hide_banner",
      "-acodec",
      "libvorbis",
      "-aq",
      "3",
      "-vn",
      "-ac",
      "2"
    );
  }
}
