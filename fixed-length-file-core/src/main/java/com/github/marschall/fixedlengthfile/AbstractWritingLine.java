package com.github.marschall.fixedlengthfile;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

abstract class AbstractWritingLine implements WritingLine {
  
  // abstract methods

  abstract void writePaddingNumber(int offset, int padding);

  abstract void writeCharAt(int index, char c);

  abstract void writePaddingString(int offset, int padding);

  // utility methods


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
    return 10;
  }

  static int digits(long l) {
    if (l < 0) {
      throw new IllegalArgumentException("value must be positive");
    }
    long p = 10L;
    for (int j = 1; j < 19; j++) {
      if (l < p) {
        return j;
      }
      p = 10 * p;
    }
    return 19;
  }

  // business methods

  @Override
  public void writeUnsignedInt(UnsignedFieldDefinition field, int value) {
    int offset = field.getOffset();
    int length = field.getLength();
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
  public void writeSegmentIndicator(StringFieldDefinition field, SegmentIndicator indicator) {
    int offset = field.getOffset();
    char c = indicator.getValue();
    writeCharAt(offset, c);
  }

  @Override
  public void writeNoValue(StringFieldDefinition field) {
    int offset = field.getOffset();
    int length = field.getLength();
    this.writePaddingString(offset, length);
  }

}
