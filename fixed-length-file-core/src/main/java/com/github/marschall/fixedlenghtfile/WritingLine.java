package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

public interface WritingLine {

  void writeUnsignedInt(UnsignedFieldDefinition field, int value);

  void writeUnsignedLong(UnsignedFieldDefinition field, long value);

  void writeString(StringFieldDefinition field, String s);

  void writeSegmentIndicator(StringFieldDefinition field, SegmentIndicator indicator);

}
