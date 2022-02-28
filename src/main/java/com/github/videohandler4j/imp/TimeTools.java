package com.github.videohandler4j.imp;

import static com.github.utils4j.imp.Strings.padStart;
import static com.github.utils4j.imp.Strings.toInt;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.github.utils4j.imp.Args;
import com.github.utils4j.imp.Strings;
import com.github.videohandler4j.IVideoFile;
import com.github.videohandler4j.IVideoSlice;

public class TimeTools {
  public static final Duration ONE_HOUR         = Duration.ofHours(1);
  public static final Duration ONE_MINUTE       = Duration.ofMinutes(1);
  public static final Duration ONE_SECOND       = Duration.ofSeconds(1);
  public static final Duration ONE_MILLISECOND  = Duration.ofMillis(1);

  private TimeTools() {}
  
  public static String toString(long timeMillis) {
    return toString(timeMillis, ':', ':');
  }
  
  public static String toHmsString(long timeMillis) {
    return toString(timeMillis, 'h', 'm') + 's';
  }
  
  public static String toString(long timeMillis, char hseparator, char mseparator) {
    long div = timeMillis / ONE_HOUR.toMillis();
    long mod = timeMillis % ONE_HOUR.toMillis();
    if (mod == 0) {
      return padStart(div, 2) + hseparator + "00" + mseparator + "00";
    }
    long hours = div;
    timeMillis -= hours * ONE_HOUR.toMillis();
    div = timeMillis / ONE_MINUTE.toMillis();
    mod = timeMillis % ONE_MINUTE.toMillis();
    if (mod == 0) {
      return padStart(hours, 2) + hseparator + padStart(div, 2) + mseparator + "00";
    }
    long minutes = div;
    timeMillis -= minutes * ONE_MINUTE.toMillis();
    div = timeMillis / ONE_SECOND.toMillis();
    return padStart(hours, 2) + hseparator + padStart(minutes, 2) + mseparator + padStart(div, 2);
  }
  
  public static long toMillis(String time) {
    if (!Strings.hasText(time))
      return -1;
    final int length = time.length();
    int start = 0;
    int next = time.indexOf(':');
    if (next <= 0)
      return -1;
    int hour = toInt(time.substring(start, next), -1);
    if (hour < 0)
      return -1;
    Duration h = ofHours(hour);
    start = next + 1;
    if (start >= length)
      return -1;
    next = time.indexOf(':', start);
    if (next < 0)
      return -1;
    int minutes = toInt(time.substring(start, next), -1);
    if (minutes < 0)
      return -1;
    Duration m = ofMinutes(minutes);
    start = next + 1;
    if (start >= length)
      return -1;
    int sec = toInt(time.substring(start), -1);
    if (sec < 0)
      return -1;
    Duration s = ofSeconds(sec);
    return h.toMillis() + m.toMillis() + s.toMillis();
  }

  public static IVideoSlice[] slice(IVideoFile file, Duration maxDuration) {
    return slice(file, maxDuration, 0);
  }
  
  public static IVideoSlice[] slice(IVideoFile file, Duration maxDuration, long begin) {
    Args.requireNonNull(maxDuration, "maxDuration is null");
    Args.requireNonNull(file, "file is null");
    List<IVideoSlice> slices = new ArrayList<>();
    long durationVideo = file.getDuration();
    long durationSlice = maxDuration.toMillis();
    long lastTime = begin;
    for(long start = begin; start < durationVideo; start += durationSlice) {
      slices.add(new VideoSlice(start, lastTime = start + durationSlice));
    }
    if (lastTime < durationVideo) {
      slices.add(new VideoSlice(lastTime, durationVideo));
    }
    return slices.toArray(new IVideoSlice[slices.size()]);
  }

  public static IVideoSlice[] slice(IVideoFile file, long maxSize) {
    return slice(file, maxSize, 0);
  }
  
  public static IVideoSlice[] slice(IVideoFile file, long maxSize, long begin) {
    Args.requireNonNull(maxSize, "maxDuration is null");
    Args.requireNonNull(file, "file is null");
    long duration = file.getDuration();
    long size = file.length();
    long sizePerTime = size / duration;
    long sliceDuration = maxSize / sizePerTime;
    return slice(file, Duration.ofMillis(sliceDuration), begin);
  }
}

