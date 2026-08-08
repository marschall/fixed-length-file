package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FileDefinition.FieldAndOffset;

public abstract sealed class RecordDefinition {

  private final String type;
  private final Map<FieldDefinition, FieldAndOffset> fieldMap;

  RecordDefinition(String type, List<FieldAndOffset> records) {
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

  abstract int getMaxiumLength();

  static int computeLength(List<FieldAndOffset> records) {
    int totalLength = 0;
    for (FieldAndOffset record : records) {
      totalLength += record.length();
    }
    return totalLength;
  }

  String getType() {
    return this.type;
  }

  private FieldAndOffset getRequiredFieldDefinition(FieldDefinition definition) {
    FieldAndOffset fieldAndOffset = this.fieldMap.get(definition);
    if (fieldAndOffset == null) {
      throw new IllegalArgumentException("Field " + definition.getName() + " not present in record: " + this.type);
    }
    return fieldAndOffset;
  }

  BoundIntegerField bindUnsingedIntegerField(UnsignedFieldDefinition definition) {
    FieldAndOffset fieldAndOffset = this.getRequiredFieldDefinition(definition);
    return new BoundIntegerField(fieldAndOffset.offset(), definition);
  }

  BoundLongField bindUnsignedLongField(UnsignedFieldDefinition definition) {
    FieldAndOffset fieldAndOffset = this.getRequiredFieldDefinition(definition);
    return new BoundLongField(fieldAndOffset.offset(), definition);
  }

  BoundStringField bindStringField(StringFieldDefinition definition) {
    FieldAndOffset fieldAndOffset = this.getRequiredFieldDefinition(definition);
    return new BoundStringField(fieldAndOffset.offset(), definition);
  }

  BoundSegmentIndicatorField bindSegmentIndicatorField(StringFieldDefinition definition) {
    if (definition.getLength() != 1) {
      throw new IllegalArgumentException("Segment indicator must have length 1");
    }
    FieldAndOffset fieldAndOffset = this.getRequiredFieldDefinition(definition);
    return new BoundSegmentIndicatorField(fieldAndOffset.offset(), definition);
  }

  @Override
  public String toString() {
    return "RecordType(" + this.type + ")";
  }

  abstract int determineRecordLength(MemorySegment memorySegment, long lineStart);

  public static final class FixedLengthRecordDefinition extends RecordDefinition {

    private final int length;

    FixedLengthRecordDefinition(String type, List<FieldAndOffset> records) {
      super(type, records);
      this.length = computeLength(records);
    }
    
    @Override
    int getMaxiumLength() {
      return this.length;
    }

    @Override
    int determineRecordLength(MemorySegment memorySegment, long lineStart) {
      return this.getMaxiumLength();
    }

  }

  public static final class SegmentedRecordDefinition extends RecordDefinition {

    private final int baseLength;
    private final int maxiumLength;

    private final List<SegmentDefinition> segmentDefinitions;

    SegmentedRecordDefinition(String type, List<FieldAndOffset> fixedRecords, List<SegmentDefinition> segmentDefinitions) {
      super(type, fixedRecords);
      this.segmentDefinitions = segmentDefinitions;
      this.baseLength = computeLength(fixedRecords);
      this.maxiumLength = computeMaxiumLength(fixedRecords, segmentDefinitions);
    }

    static int computeMaxiumLength(List<FieldAndOffset> fixedRecords, List<SegmentDefinition> segmentDefinitions) {
      int maxLength = computeLength(fixedRecords);
      for (SegmentDefinition segmentDefinition : segmentDefinitions) {
        maxLength += segmentDefinition.getLength();
      }
      return maxLength;
    }
    
    @Override
    int getMaxiumLength() {
      return this.maxiumLength;
    }

    @Override
    int determineRecordLength(MemorySegment memorySegment, long lineStart) {
      // TODO Move to fixed length file?
      int recordLength = this.baseLength;
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

    private final StringFieldDefinition segmentIndicatorField;

    private final List<FieldAndOffset> fields;

    private final int length;

    private final int segmentIndex;

    SegmentDefinition(int segmentIndex, StringFieldDefinition segmentIndicatorField, List<FieldAndOffset> fields) {
      this.segmentIndex = segmentIndex;
      this.segmentIndicatorField = Objects.requireNonNull(segmentIndicatorField, "segment indicator field");
      this.fields = fields;
      this.length = computeLength(fields);
    }

    int getLength() {
      return this.length;
    }

    @Override
    public String toString() {
      return "Segement(name=" + this.segmentIndicatorField.getName() + ", length" + this.length + ")";
    }

  }

}
