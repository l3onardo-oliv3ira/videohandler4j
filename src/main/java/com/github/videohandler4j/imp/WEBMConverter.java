package com.github.videohandler4j.imp;

import java.io.File;
import java.util.List;

import com.github.filehandler4j.IInputFile;
import com.github.utils4j.imp.Containers;

public class WEBMConverter extends FFMPEGHandler {

  @Override
  protected final List<String> getCommandLine(File ffmpegPath, IInputFile file) {
    return Containers.arrayList(
      ffmpegPath.getAbsolutePath(),
      "-y",
      "-i",
      file.getAbsolutePath(),
      "-hide_banner",
      "-map",
      "0:v:0",
      "-map",
      "0:a:0",
      "-c:v",
      "libvpx",
      "-b:v",
      "512k",
      "-crf",
      "40",
      "-max_muxing_queue_size",
      "89478485",
      "-c:a",
      "libvorbis",
      "-ac",
      "2",
      "-strict",
      "experimental",
      "-vf",
      "scale='-1:min(ih,320)'"
    );
  }
}
