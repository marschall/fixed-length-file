package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

final class FixedLatin1MemorySegmentReadingLine extends Latin1MemorySegmentReadingLine {

  FixedLatin1MemorySegmentReadingLine(MemorySegment segment) {
    super(segment);
  }

  @Override
  public int readUnsignedInt(StringFieldDefinition segmentField, UnsignedFieldDefinition field) {
    throw new UnsupportedOperationException("not segmented record");
  }

  @Override
  public long readUnsignedLong(StringFieldDefinition segmentField, UnsignedFieldDefinition field) {
    throw new UnsupportedOperationException("not segmented record");
  }

  @Override
  public String readTrimmedString(StringFieldDefinition segmentField, StringFieldDefinition field) {
    throw new UnsupportedOperationException("not segmented record");
  }


}
