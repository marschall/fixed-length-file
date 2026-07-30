package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

final class Latin1MemorySegmentReadingLine implements ReadingLine {
  // TODO currently unlimited lenght, could benefit from slice()
  
  private final MemorySegment segment;
  
  private final long start;

  Latin1MemorySegmentReadingLine(MemorySegment segment, long start) {
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
    byte b = this.segment.getAtIndex(JAVA_BYTE, index);
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
    long base = this.start + offset;
    long start = base + length - 1L;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(base + i);
      if (c != ' ') {
        start = base + i;
        break;
      }
    }
    if (start == base + length - 1L) {
      // avoid allocation for the common case of an empty string
      return "";
    }
    long end = base + length - 1L;
    for (long l = end; l >= start; l--) {
      char c = readCharAt(l);
      if (c != ' ') {
        end = l;
        break;
      }
    }
    int bufferLength = (int) (end - start) + 1;
    byte[] buffer = new byte[bufferLength];
    MemorySegment.copy(this.segment, JAVA_BYTE, start, buffer, 0, bufferLength);
    // REVIEW this.segment.asSlice().getString() would avoid one copy
    return new String(buffer, 0, bufferLength, StandardCharsets.ISO_8859_1);
  }

}
