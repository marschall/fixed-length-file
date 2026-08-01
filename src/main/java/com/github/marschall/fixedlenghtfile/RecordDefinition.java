package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;

public abstract sealed class RecordDefinition {

  private final String prefix;
  final int length;

  RecordDefinition(String prefix, int length) {
    this.length = length;
    this.prefix = Objects.requireNonNull(prefix, "prefix");
  }

  String getPrefix() {
    return this.prefix;
  }
  
  @Override
  public String toString() {
    return "RecordType(" + this.prefix + ")";
  }

  abstract int determineRecordLengt(MemorySegment memorySegment, long lineStart);

  public static final class FixedLengthRecordDefinition extends RecordDefinition {

    FixedLengthRecordDefinition(String prefix, int length) {
      super(prefix, length);
    }

    @Override
    int determineRecordLengt(MemorySegment memorySegment, long lineStart) {
      return this.length;
    }

  }

  public static final class SegmentedRecordDefinition extends RecordDefinition {

    private final List<SegmentDefinition> segmentDefinitions;

    SegmentedRecordDefinition(String prefix, int baseLength, List<SegmentDefinition> segmentDefinitions) {
      super(prefix, baseLength);
      this.segmentDefinitions = segmentDefinitions;
    }

    @Override
    int determineRecordLengt(MemorySegment memorySegment, long lineStart) {
      // TODO Move to fixed length file?
      int recordLength = this.length;
      for (SegmentDefinition segmentDefinition : this.segmentDefinitions) {
        if (this.isSegmentPresentInLine(memorySegment, lineStart, segmentDefinition)) {
          recordLength += segmentDefinition.getLength();
        }
      }
      return recordLength;
    }

    private boolean isSegmentPresentInLine(MemorySegment memorySegment, long lineStart, SegmentDefinition segmentDefinition) {
      byte b = memorySegment.getAtIndex(JAVA_BYTE, lineStart + segmentDefinition.offset);
      char c = (char) Byte.toUnsignedInt(b);
      return switch (c) {
        case SegmentIndicator.PRESENT_VALUE, SegmentIndicator.SPACES_VALUE -> true;
        case SegmentIndicator.ABSENT_VALUE -> false;
        default -> throw new FileFormatException("Unexpected segment indicator: " + c);
      };
    }

  }

  static final class SegmentDefinition {

    private final int offset;
    private final int length;

    SegmentDefinition(int offset, int length) {
      if (offset < 0) {
        throw new IllegalArgumentException();
      }
      if (length < 0) {
        throw new IllegalArgumentException();
      }
      this.offset = offset;
      this.length = length;
    }

    int getOffset() {
      return this.offset;
    }

    int getLength() {
      return this.length;
    }
    
    @Override
    public String toString() {
      return "Segement(offset=" + this.offset + ", length" + this.length + ")";
    }

  }

}
