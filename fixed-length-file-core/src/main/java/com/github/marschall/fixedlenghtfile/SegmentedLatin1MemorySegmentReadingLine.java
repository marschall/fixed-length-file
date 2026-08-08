package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;

import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentedField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;

final class SegmentedLatin1MemorySegmentReadingLine extends Latin1MemorySegmentReadingLine {
  
  private static final int SEGMENT_NOT_PRESENT = -1;
  private static final int SEGMENT_IS_SPACES = -2;

  SegmentedLatin1MemorySegmentReadingLine(MemorySegment segment) {
    super(segment);
  }
  
  private int getSegmentStart(BoundSegmentIndicatorField segmentIndicator) {
    
  }

  @Override
  public int readUnsignedInt(BoundSegmentedField<BoundIntegerField> field) {
    BoundSegmentIndicatorField segmentIndicator = field.getSegmentIndicator();
    int segmentStart = getSegmentStart(segmentIndicator);
    return switch (segmentStart) {
      case SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SEGMENT_IS_SPACES -> 0;
      default -> this.readUnsignedInt(segmentStart, field.getUnderlyingField());
    };
  }

  @Override
  public long readUnsignedLong(BoundSegmentedField<BoundLongField> field) {
    BoundSegmentIndicatorField segmentIndicator = field.getSegmentIndicator();
    int segmentStart = getSegmentStart(segmentIndicator);
    return switch (segmentStart) {
      case SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SEGMENT_IS_SPACES -> 0L;
      default -> this.readUnsignedLong(segmentStart, field.getUnderlyingField());
    };
  }

  @Override
  public String readTrimmedString(BoundSegmentedField<BoundStringField> field) {

    BoundSegmentIndicatorField segmentIndicator = field.getSegmentIndicator();
    int segmentStart = getSegmentStart(segmentIndicator);
    return switch (segmentStart) {
      case SEGMENT_NOT_PRESENT -> throw new IllegalStateException("segment not present");
      case SEGMENT_IS_SPACES -> "";
      default -> this.readTrimmedString(segmentStart, field.getUnderlyingField());
    };
  }

}
