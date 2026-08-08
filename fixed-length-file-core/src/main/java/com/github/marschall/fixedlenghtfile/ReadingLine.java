package com.github.marschall.fixedlenghtfile;

import java.io.Reader;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentedField;

public interface ReadingLine {

  int readUnsignedInt(BoundIntegerField field);

  long readUnsignedLong(BoundLongField field);

  String readTrimmedString(BoundStringField field);

  int readUnsignedInt(BoundSegmentedField<BoundIntegerField> field);

  long readUnsignedLong(BoundSegmentedField<BoundLongField> field);

  String readTrimmedString(BoundSegmentedField<BoundStringField> field);

  SegmentIndicator readSegmentIndicator(BoundSegmentIndicatorField field);

  // in char
  int getLength();
  
  // see java.sql.PreparedStatement#setCharacterStream(int, Reader, int)
  Reader asReader();

}
