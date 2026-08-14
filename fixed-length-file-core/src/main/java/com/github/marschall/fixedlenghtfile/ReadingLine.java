package com.github.marschall.fixedlenghtfile;

import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

public interface ReadingLine {

  int readUnsignedInt(UnsignedFieldDefinition field);

  long readUnsignedLong(UnsignedFieldDefinition field);

  String readTrimmedString(StringFieldDefinition field);

  LocalDate readLocalDate(UnsignedFieldDefinition field);

  LocalTime readLocalTime(UnsignedFieldDefinition field);

  LocalDateTime readLocalDateTime(UnsignedFieldDefinition dateField, UnsignedFieldDefinition timeField);
  
  BigDecimal readBigDecimal(UnsignedFieldDefinition amountField, UnsignedFieldDefinition exponentField);
  
  // segmented access

  int readUnsignedInt(StringFieldDefinition segmentField, UnsignedFieldDefinition field);

  long readUnsignedLong(StringFieldDefinition segmentField, UnsignedFieldDefinition field);

  String readTrimmedString(StringFieldDefinition segmentField, StringFieldDefinition field);

  SegmentIndicator readSegmentIndicator(StringFieldDefinition field);

  // in char
  int getLength();
  
  // see java.sql.PreparedStatement#setCharacterStream(int, Reader, int)
  Reader asReader();

}
