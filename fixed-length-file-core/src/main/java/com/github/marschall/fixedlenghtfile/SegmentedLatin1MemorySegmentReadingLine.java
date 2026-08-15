package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;

final class SegmentedLatin1MemorySegmentReadingLine extends Latin1MemorySegmentReadingLine {
  
  private final SegmentOffsets segmentOffsets;

  SegmentedLatin1MemorySegmentReadingLine(MemorySegment segment, SegmentOffsets segmentOffsets) {
    super(segment);
    this.segmentOffsets = Objects.requireNonNull(segmentOffsets, "segmentOffsets");
  }
  
  private int getSegmentStart(SegmentFieldDefinition<?> field) {
    return this.segmentOffsets.getSegmentOffset(field.getSegmentIndex());
  }

  @Override
  public int readUnsignedInt(SegmentFieldDefinition<UnsignedFieldDefinition> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0;
      default -> this.readUnsignedInt(segmentStart, field.getDelegate());
    };
  }

  @Override
  public long readUnsignedLong(SegmentFieldDefinition<UnsignedFieldDefinition> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0L;
      default -> this.readUnsignedLong(segmentStart, field.getDelegate());
    };
  }

  @Override
  public String readTrimmedString(SegmentFieldDefinition<StringFieldDefinition> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> "";
      default -> this.readTrimmedString(segmentStart, field.getDelegate());
    };
  }

}
