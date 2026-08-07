package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.FileDefinition.FieldAndOffset;

public abstract sealed class RecordDefinition {

  private final String type;
  private final int length;
  private final Map<FieldDefinition, FieldAndOffset> fieldMap;

  RecordDefinition(String type, List<FieldAndOffset> records) {
    this.length = computeLength(records);
    this.type = Objects.requireNonNull(type, "type");
    this.fieldMap = buildFieldMap(records);
  }

  private static Map<FieldDefinition, FieldAndOffset> buildFieldMap(List<FieldAndOffset> records) {
    Map<FieldDefinition, FieldAndOffset> map = HashMap.newHashMap(records.size());
    for (FieldAndOffset record : records) {
      map.put(record.definition(), record);
    }
    return map;
  }
  
  int getLength() {
    return this.length;
  }

  private static int computeLength(List<FieldAndOffset> records) {
    int totalLength = 0;
    for (FieldAndOffset record : records) {
      totalLength += record.length();
    }
    return totalLength;
  }

  String getType() {
    return this.type;
  }
  
  @Override
  public String toString() {
    return "RecordType(" + this.type + ")";
  }

  abstract int determineRecordLengt(MemorySegment memorySegment, long lineStart);

  public static final class FixedLengthRecordDefinition extends RecordDefinition {

    FixedLengthRecordDefinition(String type, List<FieldAndOffset> records) {
      super(type, records);
    }

    @Override
    int determineRecordLengt(MemorySegment memorySegment, long lineStart) {
      return this.getLength();
    }

  }

  public static final class SegmentedRecordDefinition extends RecordDefinition {

    private final List<SegmentDefinition> segmentDefinitions;

    SegmentedRecordDefinition(String type, List<FieldAndOffset> fixedRecords, List<SegmentDefinition> segmentDefinitions) {
      super(type, fixedRecords);
      this.segmentDefinitions = segmentDefinitions;
    }

    @Override
    int determineRecordLengt(MemorySegment memorySegment, long lineStart) {
      // TODO Move to fixed length file?
      int recordLength = this.getLength();
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
