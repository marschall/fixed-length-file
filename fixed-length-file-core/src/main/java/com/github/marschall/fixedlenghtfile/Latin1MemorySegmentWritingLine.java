package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;

final class Latin1MemorySegmentWritingLine implements WritingLine {
  // TODO currently unlimited length, could benefit from slice()
  
  private final MemorySegment segment;
  
  private final long start;

  Latin1MemorySegmentWritingLine(MemorySegment segment, long start) {
    this.segment = segment;
    this.start = start;
  }

  @Override
  public void writeUnsignedInt(BoundIntegerField field, int value) {
    int offset = field.getOffset();
    int length = field.getLength();
    // RREVIEW other option
    // buffer = toLatin1ByteArray(value);
    // .asSlice(base, padding).fill((byte) '0');
    // MemorySegment.copy(buffer, 0, this.segment, ValueLayout.JAVA_BYTE, base + padding, buffer.length);
    int digits = digits(value);
    long base = this.start + offset;
    int padding = length - digits;
    for (int i = 0; i < padding; i++) {
      writeCharAt(base + i, '0');
    }
    int remaining = value;
    for (int i = 0; i < digits; i++) {
      int digit = remaining % 10;
      writeCharAt(base + length - i - 1, (char) ('0' + digit));
      remaining = remaining / 10;
    }
  }

  @Override
  public void writeUnsignedLong(BoundLongField field, long value) {
    int offset = field.getOffset();
    int length = field.getLength();
    // TODO Auto-generated method stub
    
  }

  @Override
  public void writeString(BoundStringField field, String s) {
    int offset = field.getOffset();
    int length = field.getLength();
    // REVIEW this.segment.setString will add 0 terminator
    long base = this.start + offset;
    if (s != null) {
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c > 255) {
          throw new IllegalArgumentException("non-latin 1 character encountered");
        }
        writeCharAt(base + i, c);
      }
    }
    int stringLength = s != null ? s.length() : 0;
    int padding = length - stringLength;
    for (int i = 0; i < padding; i++) {
      // .asSlice(base, padding).fill((byte) ' ');
      writeCharAt(base + stringLength + i, ' ');
    }
  }
  
  @Override
  public void writeSegmentIndicator(BoundSegmentIndicatorField field, SegmentIndicator indicator) {
    int offset = field.getOffset();
    long base = this.start + offset;
    char c = indicator.getValue();
    writeCharAt(base, c);
  }

  private void writeCharAt(long index, char c) {
    byte b = (byte) c;
    this.segment.setAtIndex(JAVA_BYTE, index, b);
  }
  
  private static byte[] toLatin1ByteArray(int i) {
    // REVIEW could be pooled
    int digits = digits(i);
    byte[] buffer = new byte[digits];
    int remaining = i;
    for (int j = 0; j < digits; j++) {
      int digit = remaining % 10;
      buffer[digits - j - 1] = (byte) ('0' + digit);
      remaining = remaining / 10;
    }
    return buffer;
  }

  static int digits(int i) {
    if (i < 0) {
      throw new IllegalArgumentException("value must be positive");
    }
    int p = 10;
    for (int j = 1; j < 10; j++) {
        if (i < p) {
          return j;
        }
        p = 10 * p;
    }
    return 9;
  }

}
