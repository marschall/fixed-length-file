package com.github.marschall.fixedlenghtfile;

import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;

public interface ReadingLine {

  int readUnsignedInt(UnsignedFieldDefinition field);

  long readUnsignedLong(UnsignedFieldDefinition field);

  String readTrimmedString(StringFieldDefinition field);

  LocalDate readLocalDate(UnsignedFieldDefinition field);

  LocalTime readLocalTime(UnsignedFieldDefinition field);

  LocalDateTime readLocalDateTime(UnsignedFieldDefinition dateField, UnsignedFieldDefinition timeField);
  
  BigDecimal readBigDecimal(UnsignedFieldDefinition amountField, UnsignedFieldDefinition exponentField);
  
  // segmented access

  int readUnsignedInt(SegmentFieldDefinition<UnsignedFieldDefinition> field);

  long readUnsignedLong(SegmentFieldDefinition<UnsignedFieldDefinition>  field);

  String readTrimmedString(SegmentFieldDefinition<StringFieldDefinition> field);

  SegmentIndicator readSegmentIndicator(StringFieldDefinition field);

  // in char
  int getLength();
  
  // see java.sql.PreparedStatement#setCharacterStream(int, Reader, int)
  Reader asReader();

}
