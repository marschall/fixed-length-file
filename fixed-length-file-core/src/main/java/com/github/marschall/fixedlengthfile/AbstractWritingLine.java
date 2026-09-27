package com.github.marschall.fixedlengthfile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

abstract class AbstractWritingLine implements WritingLine {
  
  private SegmentOffsets segmentOffsets;
  
  AbstractWritingLine() {
    this.segmentOffsets = SegmentOffsets.NoSegment.INSTANCE;
  }
  
  void setSegmentsOffsets(SegmentOffsets segmentOffsets) {
    this.segmentOffsets = segmentOffsets;
  }
  
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

  protected abstract void doSetLength(int recordLength);

  // public methods

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
  
  // high level public methods

  @Override
  public void writeLocalDate(UnsignedFieldDefinition field, LocalDate value) {
    int yyyyMMdd = value.getYear() * 10000
        + value.getMonthValue() * 100
        + value.getDayOfMonth();
    writeUnsignedInt(field, yyyyMMdd);
  }

  @Override
  public void writeLocalTime(UnsignedFieldDefinition field, LocalTime value) {
    int hhmmss = value.getHour() * 10000
        + value.getMinute() * 100
        + value.getMinute();
    if (field.getLength() == 8) {
      int hhmmsscc = hhmmss * 100 + value.getNano() / 10_000_00;
      writeUnsignedInt(field, hhmmsscc);
    } else {
      writeUnsignedInt(field, hhmmss);
    }
  }

  @Override
  public void writeLocalDateTime(UnsignedFieldDefinition dateField, UnsignedFieldDefinition timeField, LocalDateTime value) {
    writeLocalDate(dateField, value.toLocalDate());
    writeLocalTime(timeField, value.toLocalTime());
  }

  @Override
  public void writeBigDecimal(UnsignedFieldDefinition amountField, UnsignedFieldDefinition exponentField,
      BigDecimal value, int scale) {
    BigDecimal fieldValue = value.movePointRight(scale);
    if (amountField.getLength() <= 9) {
      writeUnsignedInt(amountField, fieldValue.intValueExact());
    } else {
      writeUnsignedLong(amountField, fieldValue.longValueExact());
    }
    writeUnsignedInt(exponentField, scale);
  }

}
