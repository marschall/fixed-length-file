package com.github.marschall.fixedlengthfile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

public interface WritingLine {
  
  // TODO LocalDate, LocalTime, LocalDateTime, BigDecimal

  void writeUnsignedInt(UnsignedFieldDefinition field, int value);

  void writeUnsignedLong(UnsignedFieldDefinition field, long value);

  void writeString(StringFieldDefinition field, String s);
  
  void writeNoValue(UnsignedFieldDefinition field);
  
  void writeNoValue(StringFieldDefinition field);

  void writeSegmentIndicator(StringFieldDefinition field, SegmentIndicator indicator);
  
  // high level methods

  void writeLocalDate(UnsignedFieldDefinition field, LocalDate value);

  void writeLocalTime(UnsignedFieldDefinition field, LocalTime value);

  void writeLocalDateTime(UnsignedFieldDefinition dateField, UnsignedFieldDefinition timeField, LocalDateTime value);

  void writeBigDecimal(UnsignedFieldDefinition amountField, UnsignedFieldDefinition exponentField, BigDecimal value, int scale);

}
