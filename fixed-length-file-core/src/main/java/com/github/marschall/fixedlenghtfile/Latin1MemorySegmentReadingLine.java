package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;

final class Latin1MemorySegmentReadingLine implements ReadingLine {

  private final MemorySegment segment;

  Latin1MemorySegmentReadingLine(MemorySegment segment, long start) {
    this(segment);
  }

  Latin1MemorySegmentReadingLine(MemorySegment segment) {
    this.segment = segment;
  }

  @Override
  public int readUnsignedInt(BoundIntegerField field) {
    int offset = field.getOffset();
    int length = field.getLength();
    int value = 0;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c < '0' || c > '9') {
        throw digitExpectedAt(offset + i, c);
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
    return new FileFormatException("expected digit at index: " + i + " but got: " + c);
  }

  @Override
  public long readUnsignedLong(BoundLongField field) {
    int offset = field.getOffset();
    int length = field.getLength();
    long value = 0L;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c < '0' || c > '9') {
        throw digitExpectedAt(offset + i, c);
      }
      value = value * 10L + (c - '0');
    }
    return value;
  }

  @Override
  public String readTrimmedString(BoundStringField field) {
    int offset = field.getOffset();
    int length = field.getLength();
    long start = offset + length - 1L;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c != ' ') {
        start = offset + i;
        break;
      }
    }
    if (start == offset + length - 1L) {
      // avoid allocation for the common case of an empty string
      return "";
    }
    long end = offset + length - 1L;
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

  @Override
  public SegmentIndicator readSegmentIndicator(BoundSegmentIndicatorField field) {
    int offset = field.getOffset();
    char c = readCharAt(offset);
    return switch (c) {
      case SegmentIndicator.PRESENT_VALUE -> SegmentIndicator.PRESENT;
      case SegmentIndicator.ABSENT_VALUE -> SegmentIndicator.ABSENT;
      case SegmentIndicator.SPACES_VALUE -> SegmentIndicator.SPACES;
      default -> throw new FileFormatException("Unexpected segment indicator: " + c);
    };
  }

  @Override
  public int getLength() {
    return Math.toIntExact(this.segment.byteSize());
  }

}
