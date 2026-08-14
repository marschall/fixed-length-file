package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

final class SegmentedLatin1MemorySegmentReadingLine extends Latin1MemorySegmentReadingLine {
  
  private final SegmentOffsets segmentOffsets;

  SegmentedLatin1MemorySegmentReadingLine(MemorySegment segment, SegmentOffsets segmentOffsets) {
    super(segment);
    this.segmentOffsets = Objects.requireNonNull(segmentOffsets, "segmentOffsets");
  }
  
  private int getSegmentStart(StringFieldDefinition field) {
    return this.segmentOffsets.getSegmentOffset(field.getSegmentIndex());
  }

  @Override
  public int readUnsignedInt(StringFieldDefinition segmentField, UnsignedFieldDefinition field) {
    int segmentStart = getSegmentStart(segmentField);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0;
      default -> this.readUnsignedInt(segmentStart, field);
    };
  }

  @Override
  public long readUnsignedLong(StringFieldDefinition segmentField, UnsignedFieldDefinition field) {
    int segmentStart = getSegmentStart(segmentField);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0L;
      default -> this.readUnsignedLong(segmentStart, field);
    };
  }

  @Override
  public String readTrimmedString(StringFieldDefinition segmentField, StringFieldDefinition field) {
    int segmentStart = getSegmentStart(segmentField);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> "";
      default -> this.readTrimmedString(segmentStart, field);
    };
  }

}
