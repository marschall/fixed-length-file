package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

final class Latin1MemorySegmentLine implements Line {
  
  private final MemorySegment segment;
  
  private final long start;

  Latin1MemorySegmentLine(MemorySegment segment, long start) {
    this.segment = segment;
    this.start = start;
  }

  @Override
  public int readUnsignedIntAt(int offset, int length) {
    int value = 0;
    long base = this.start + offset;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(base + i);
      if (c < '0' || c > '9') {
        throw digitExpectedAt(base + i, c);
      }
      value = value * 10 + (c - '0');
    }
    return value;
  }

  private char readCharAt(long index) {
    byte b = this.segment.getAtIndex(ValueLayout.JAVA_BYTE, index);
    return (char) Byte.toUnsignedInt(b);
  }

  private static RuntimeException digitExpectedAt(long i, char c) {
    return new IllegalArgumentException("expected digit at index: " + i + " but got: " + c);
  }

  @Override
  public long readUnsignedLongAt(int offset, int length) {
    long value = 0L;
    long base = this.start + offset;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(base + i);
      if (c < '0' || c > '9') {
        throw digitExpectedAt(base + i, c);
      }
      value = value * 10L + (c - '0');
    }
    return value;
  }

  @Override
  public String readTrimmedStringAt(int offset, int length) {
    // TODO check for empty
    byte[] buffer = new byte[length];
    int bufferLength = 0;
    return new String(buffer, 0, bufferLength, StandardCharsets.ISO_8859_1);
  }

}
