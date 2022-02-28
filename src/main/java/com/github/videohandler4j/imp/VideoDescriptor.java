package com.github.videohandler4j.imp;

import java.io.File;
import java.io.IOException;

import com.github.filehandler4j.imp.InputDescriptor;
import com.github.videohandler4j.IVideoFile;

public class VideoDescriptor extends InputDescriptor {
  
  public VideoDescriptor() {
  }

  public static class Builder extends InputDescriptor.Builder{
    
    public Builder(String extension) {
      super(extension);
    }
    
    @Override
    public Builder add(File input) {
      super.add(VideoTool.FFMPEG.call(input));
      return this;
    }
    
    public Builder add(IVideoFile input) {
      super.add(input);
      return this;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public VideoDescriptor build() throws IOException {
      return (VideoDescriptor)super.build();
    }
    
    @Override
    protected VideoDescriptor createDescriptor() {
      return new VideoDescriptor();
    }
  }
}
