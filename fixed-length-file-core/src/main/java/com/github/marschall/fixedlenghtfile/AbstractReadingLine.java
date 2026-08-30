package com.github.marschall.fixedlenghtfile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;

abstract class AbstractReadingLine implements ReadingLine {

  protected AbstractReadingLine() {
    super();
  }
  
  // higher type methods

  @Override
  public LocalDate readLocalDate(UnsignedFieldDefinition field) {
    int yyyyMMdd = readUnsignedInt(field);
    if (yyyyMMdd < 1000_00_00) {
      // TODO invalid
      return null;
    }
    int dayOfMonth = yyyyMMdd % 100;
    int month = (yyyyMMdd / 100) % 100;
    int year = yyyyMMdd / 100_00;
    return LocalDate.of(year, month, dayOfMonth);
  }

  @Override
  public LocalTime readLocalTime(UnsignedFieldDefinition field) {
    int length = field.getLength();
    int value = readUnsignedInt(field);
    // length 6: hhmmss 8: hhmmsscc
    int hhmmsscc = length == 6 ? value * 100 : value;
    int nanoOfSecond = (hhmmsscc % 100) * 10_000_000; // xx -> xx0_000_000
    int second = (hhmmsscc / 100) % 100;
    int minute = (hhmmsscc / 100_00) % 100;
    int hour = hhmmsscc / 100_00_00;
    return LocalTime.of(hour, minute, second, nanoOfSecond);
  }

  @Override
  public LocalDateTime readLocalDateTime(UnsignedFieldDefinition dateField, UnsignedFieldDefinition timeField) {
    var localDate = readLocalDate(dateField);
    if (localDate == null) {
      // TODO invalid
      return null;
    }
    var localTime = readLocalTime(timeField);
    return LocalDateTime.of(localDate, localTime);
  }

  @Override
  public BigDecimal readBigDecimal(UnsignedFieldDefinition amountField, UnsignedFieldDefinition exponentField) {
    long amount = readUnsignedLong(amountField);
    int exponent = readUnsignedInt(exponentField);
    return BigDecimal.valueOf(amount, exponent);
  }
  
  // segment methods

  protected abstract int readUnsignedInt(int segmentStart, UnsignedFieldDefinition delegate);
  
  protected abstract long readUnsignedLong(int segmentStart, UnsignedFieldDefinition delegate);
  
  protected abstract String readTrimmedString(int segmentStart, StringFieldDefinition delegate);

  protected abstract SegmentOffsets getSegmentOffsets();

  SegmentIndicator toSegmentIndicator(char c) {
    return switch (c) {
      case SegmentIndicator.PRESENT_VALUE -> SegmentIndicator.PRESENT;
      case SegmentIndicator.ABSENT_VALUE -> SegmentIndicator.ABSENT;
      case SegmentIndicator.SPACES_VALUE -> SegmentIndicator.SPACES;
      default -> throw new FileFormatException("Unexpected segment indicator: " + c);
    };
  }

  private int getSegmentStart(SegmentFieldDefinition<?> field) {
    return this.getSegmentOffsets().getSegmentOffset(field.getSegmentIndex());
  }

  @Override
  public int readUnsignedInt(SegmentFieldDefinition<UnsignedFieldDefinition> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0;
      default -> this.readUnsignedInt(segmentStart, field.getDelegate());
    };
  }

  @Override
  public long readUnsignedLong(SegmentFieldDefinition<UnsignedFieldDefinition> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0L;
      default -> this.readUnsignedLong(segmentStart, field.getDelegate());
    };
  }

  @Override
  public String readTrimmedString(SegmentFieldDefinition<StringFieldDefinition> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> "";
      default -> this.readTrimmedString(segmentStart, field.getDelegate());
    };
  }

}
