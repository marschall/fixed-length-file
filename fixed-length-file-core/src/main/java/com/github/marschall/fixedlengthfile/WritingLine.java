package com.github.marschall.fixedlengthfile;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

public interface WritingLine {

  void writeUnsignedInt(UnsignedFieldDefinition field, int value);

  void writeUnsignedLong(UnsignedFieldDefinition field, long value);

  void writeString(StringFieldDefinition field, String s);
  
  void writeNoValue(UnsignedFieldDefinition field);
  
  void writeNoValue(StringFieldDefinition field);

  void writeSegmentIndicator(StringFieldDefinition field, SegmentIndicator indicator);

}
