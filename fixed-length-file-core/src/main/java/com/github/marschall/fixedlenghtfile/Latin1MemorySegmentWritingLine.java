package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

final class Latin1MemorySegmentWritingLine implements WritingLine {

  private final MemorySegment segment;

  Latin1MemorySegmentWritingLine(MemorySegment segment) {
    this.segment = segment;
  }

  @Override
  public void writeUnsignedInt(UnsignedFieldDefinition field, int value) {
    int offset = field.getOffset();
    int length = field.getLength();
    // RREVIEW other option
    // buffer = toLatin1ByteArray(value);
    // .asSlice(base, padding).fill((byte) '0');
    // MemorySegment.copy(buffer, 0, this.segment, ValueLayout.JAVA_BYTE, base + padding, buffer.length);
    int digits = digits(value);
    int padding = length - digits;
    this.writePaddingNumber(offset, padding);
    int remaining = value;
    for (int i = 0; i < digits; i++) {
      int digit = remaining % 10;
      writeCharAt(offset + length - i - 1, (char) ('0' + digit));
      remaining = remaining / 10;
    }
  }

  private void writePaddingNumber(int offset, int padding) {
    for (int i = 0; i < padding; i++) {
      writeCharAt(offset + i, '0');
    }
  }

  @Override
  public void writeNoValue(UnsignedFieldDefinition field) {
    int offset = field.getOffset();
    int length = field.getLength();
    this.writePaddingNumber(offset, length);
  }

  @Override
  public void writeUnsignedLong(UnsignedFieldDefinition field, long value) {
    int offset = field.getOffset();
    int length = field.getLength();
    int digits = digits(value);
    int padding = length - digits;
    writePaddingNumber(offset, padding);
    long remaining = value;
    for (int i = 0; i < digits; i++) {
      int digit = (int) (remaining % 10L);
      writeCharAt(offset + length - i - 1, (char) ('0' + digit));
      remaining = remaining / 10L;
    }
  }

  @Override
  public void writeString(StringFieldDefinition field, String s) {
    int offset = field.getOffset();
    int length = field.getLength();
    // REVIEW this.segment.setString will add 0 terminator
    if (s != null) {
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c > 255) {
          throw new IllegalArgumentException("non-latin 1 character encountered");
        }
        writeCharAt(offset + i, c);
      }
    }
    int stringLength = s != null ? s.length() : 0;
    int padding = length - stringLength;
    this.writePaddingString(offset + stringLength, padding);
  }

  @Override
  public void writeNoValue(StringFieldDefinition field) {
    int offset = field.getOffset();
    int length = field.getLength();
    this.writePaddingString(offset, length);
  }

  private void writePaddingString(int offset, int padding) {
    for (int i = 0; i < padding; i++) {
      // .asSlice(base, padding).fill((byte) ' ');
      writeCharAt(offset + i, ' ');
    }
  }

  @Override
  public void writeSegmentIndicator(StringFieldDefinition field, SegmentIndicator indicator) {
    int offset = field.getOffset();
    char c = indicator.getValue();
    writeCharAt(offset, c);
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

  static int digits(long l) {
    if (l < 0) {
      throw new IllegalArgumentException("value must be positive");
    }
    int p = 10;
    for (int j = 1; j < 19; j++) {
      if (l < p) {
        return j;
      }
      p = 10 * p;
    }
    return 18;
  }

}
