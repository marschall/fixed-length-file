package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentedField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;

final class SegmentedLatin1MemorySegmentReadingLine extends Latin1MemorySegmentReadingLine {
  
  private final SegmentOffsets segmentOffsets;

  SegmentedLatin1MemorySegmentReadingLine(MemorySegment segment, SegmentOffsets segmentOffsets) {
    super(segment);
    this.segmentOffsets = Objects.requireNonNull(segmentOffsets, "segmentOffsets");
  }
  
  private int getSegmentStart(BoundSegmentedField<?> field) {
    return this.segmentOffsets.getSegmentOffset(field.getSegmentIndex());
  }

  @Override
  public int readUnsignedInt(BoundSegmentedField<BoundIntegerField> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0;
      default -> this.readUnsignedInt(segmentStart, field.getUnderlyingField());
    };
  }

  @Override
  public long readUnsignedLong(BoundSegmentedField<BoundLongField> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> 0L;
      default -> this.readUnsignedLong(segmentStart, field.getUnderlyingField());
    };
  }

  @Override
  public String readTrimmedString(BoundSegmentedField<BoundStringField> field) {
    int segmentStart = getSegmentStart(field);
    return switch (segmentStart) {
      case SegmentOffsets.SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SegmentOffsets.SEGMENT_IS_SPACES -> "";
      default -> this.readTrimmedString(segmentStart, field.getUnderlyingField());
    };
  }

}
