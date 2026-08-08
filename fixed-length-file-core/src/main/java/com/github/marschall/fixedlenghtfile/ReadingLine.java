package com.github.marschall.fixedlenghtfile;

import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.marschall.fixedlenghtfile.BoundField.BoundBigDecimalField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLocalDateTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentedField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalDateField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;

public interface ReadingLine {

  int readUnsignedInt(BoundIntegerField field);

  long readUnsignedLong(BoundLongField field);

  String readTrimmedString(BoundStringField field);

  LocalDate readLocalDate(BoundLocalDateField field);

  LocalTime readLocalTime(BoundLocalTimeField field);

  LocalDateTime readLocalDateTime(BoundLocalDateTimeField field);
  
  BigDecimal readBigDecimal(BoundBigDecimalField field);
  
  // segmented access

  int readUnsignedInt(BoundSegmentedField<BoundIntegerField> field);

  long readUnsignedLong(BoundSegmentedField<BoundLongField> field);

  String readTrimmedString(BoundSegmentedField<BoundStringField> field);

  SegmentIndicator readSegmentIndicator(BoundSegmentIndicatorField field);

  // in char
  int getLength();
  
  // see java.sql.PreparedStatement#setCharacterStream(int, Reader, int)
  Reader asReader();

}
