package com.github.marschall.fixedlenghtfile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

abstract class AbstractReadingLine implements ReadingLine {

  protected AbstractReadingLine() {
    super();
  }

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

}
