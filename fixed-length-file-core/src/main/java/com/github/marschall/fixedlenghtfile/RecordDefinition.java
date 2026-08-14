package com.github.marschall.fixedlenghtfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
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
  
  abstract int getBaseLength();

  abstract int getMaximumLength();

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

  @Override
  public String toString() {
    return "RecordType(" + this.type + ")";
  }

  public static final class FixedLengthRecordDefinition extends RecordDefinition {

    private final int length;

    FixedLengthRecordDefinition(String type, List<FieldAndOffset> records) {
      super(type, records);
      this.length = computeLength(records);
    }

    @Override
    int getBaseLength() {
      return this.length;
    }

    @Override
    int getMaximumLength() {
      return this.length;
    }

  }

  public static final class SegmentedRecordDefinition extends RecordDefinition {

    private final int baseLength;
    private final int maxiumLength;
    private final int baseRecordCount;
    private final List<SegmentDefinition> segmentDefinitions;

    SegmentedRecordDefinition(String type, List<FieldAndOffset> fixedRecords, List<SegmentDefinition> segmentDefinitions) {
      super(type, fixedRecords);
      this.segmentDefinitions = segmentDefinitions;
      this.baseLength = computeLength(fixedRecords);
      this.maxiumLength = computeMaxiumLength(fixedRecords, segmentDefinitions);
      this.baseRecordCount = fixedRecords.size();
    }

    static int computeMaxiumLength(List<FieldAndOffset> fixedRecords, List<SegmentDefinition> segmentDefinitions) {
      int maxLength = computeLength(fixedRecords);
      for (SegmentDefinition segmentDefinition : segmentDefinitions) {
        maxLength += segmentDefinition.getLength();
      }
      return maxLength;
    }

    @Override
    int getBaseLength() {
      return this.baseLength;
    }

    @Override
    int getMaximumLength() {
      return this.maxiumLength;
    }

    List<SegmentDefinition> getSegmentDefinitions() {
      return this.segmentDefinitions;
    }

    
    public int getTotalFieldCount() {
      int totalFieldCount = this.baseRecordCount;
      for (var segmentDefinition : this.segmentDefinitions) {
        totalFieldCount += segmentDefinition.getFieldCount();
      }
      return totalFieldCount;
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
    
    StringFieldDefinition getSegmentIndicatorField() {
      return this.segmentIndicatorField;
    }

    int getFieldCount() {
      return this.fields.size();
    }

    @Override
    public String toString() {
      return "Segement(name=" + this.segmentIndicatorField.getName() + ", length" + this.length + ")";
    }

  }

}
