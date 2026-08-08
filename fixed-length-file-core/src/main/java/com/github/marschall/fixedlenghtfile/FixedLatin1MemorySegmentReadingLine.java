package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;

import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentedField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;

final class FixedLatin1MemorySegmentReadingLine extends Latin1MemorySegmentReadingLine {

  FixedLatin1MemorySegmentReadingLine(MemorySegment segment) {
    super(segment);
  }

  @Override
  public int readUnsignedInt(BoundSegmentedField<BoundIntegerField> field) {
    throw new UnsupportedOperationException("not segmented record");
  }

  @Override
  public long readUnsignedLong(BoundSegmentedField<BoundLongField> field) {
    throw new UnsupportedOperationException("not segmented record");
  }

  @Override
  public String readTrimmedString(BoundSegmentedField<BoundStringField> field) {
    throw new UnsupportedOperationException("not segmented record");
  }

}
