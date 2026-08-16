package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;

final class FixedLatin1MemorySegmentReadingLine extends Latin1MemorySegmentReadingLine {

  FixedLatin1MemorySegmentReadingLine(MemorySegment segment) {
    super(segment);
  }

  @Override
  public int readUnsignedInt(SegmentFieldDefinition<UnsignedFieldDefinition> field) {
    throw new UnsupportedOperationException("not segmented record");
  }

  @Override
  public long readUnsignedLong(SegmentFieldDefinition<UnsignedFieldDefinition> field) {
    throw new UnsupportedOperationException("not segmented record");
  }

  @Override
  public String readTrimmedString(SegmentFieldDefinition<StringFieldDefinition> field) {
    throw new UnsupportedOperationException("not segmented record");
  }

}
