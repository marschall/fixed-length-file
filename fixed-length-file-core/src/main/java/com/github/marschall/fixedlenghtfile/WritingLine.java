package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

public interface WritingLine {

  void writeUnsignedInt(UnsignedFieldDefinition field, int value);

  void writeUnsignedLong(UnsignedFieldDefinition field, long value);

  void writeString(StringFieldDefinition field, String s);
  
  void writeNoValue(UnsignedFieldDefinition field);
  
  void writeNoValue(StringFieldDefinition field);

  void writeSegmentIndicator(StringFieldDefinition field, SegmentIndicator indicator);

}
