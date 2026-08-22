package com.github.marschall.fixedlenghtfile;

import java.util.List;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;

public abstract sealed class RecordDefinition {

  private final String type;

  RecordDefinition(String type) {
    this.type = Objects.requireNonNull(type, "type");
  }

  static int computeLength(List<? extends FieldDefinition> fields) {
    int totalLength = 0;
    for (FieldDefinition field : fields) {
      totalLength += field.getLength();
    }
    return totalLength;
  }

  public abstract int getBaseLength();

  public abstract int getMaximumLength();

  String getType() {
    return this.type;
  }

  @Override
  public String toString() {
    return "RecordType(" + this.type + ")";
  }

  public static final class FixedLengthRecordDefinition extends RecordDefinition {

    private final int length;
    private final List<? extends OffsetFieldDefinition> fields;

    public FixedLengthRecordDefinition(String type, List<? extends OffsetFieldDefinition> fields) {
      super(type);
      this.fields = fields;
      this.length = computeLength(fields);
    }

    @Override
    public int getBaseLength() {
      return this.length;
    }

    @Override
    public int getMaximumLength() {
      return this.length;
    }

    public List<? extends OffsetFieldDefinition> getFields() {
      return this.fields;
    }

  }

  public static final class SegmentedRecordDefinition extends RecordDefinition {

    private final int baseLength;
    private final int maxiumLength;
    private final int baseRecordCount;
    private final List<SegmentDefinition> segmentDefinitions;
    private final List<? extends OffsetFieldDefinition> fixedFields;

    public SegmentedRecordDefinition(String type, List<? extends OffsetFieldDefinition> fixedFields, List<SegmentDefinition> segmentDefinitions) {
      super(type);
      this.fixedFields = fixedFields;
      this.segmentDefinitions = segmentDefinitions;
      this.baseLength = computeLength(fixedFields);
      this.maxiumLength = computeMaxiumLength(fixedFields, segmentDefinitions);
      this.baseRecordCount = fixedFields.size();
    }

    static int computeMaxiumLength(List<? extends OffsetFieldDefinition> fixedRecords, List<SegmentDefinition> segmentDefinitions) {
      int maxLength = computeLength(fixedRecords);
      for (SegmentDefinition segmentDefinition : segmentDefinitions) {
        maxLength += segmentDefinition.getLength();
      }
      return maxLength;
    }

    @Override
    public int getBaseLength() {
      return this.baseLength;
    }

    @Override
    public int getMaximumLength() {
      return this.maxiumLength;
    }

    public List<SegmentDefinition> getSegmentDefinitions() {
      return this.segmentDefinitions;
    }

    public List<? extends OffsetFieldDefinition> getFixedFields() {
      return this.fixedFields;
    }

    
    public int getTotalFieldCount() {
      int totalFieldCount = this.baseRecordCount;
      for (var segmentDefinition : this.segmentDefinitions) {
        totalFieldCount += segmentDefinition.getFieldCount();
      }
      return totalFieldCount;
    }

  }

  public static final class SegmentDefinition {

    private final StringFieldDefinition segmentIndicatorField;

    private final List<SegmentFieldDefinition<?>> fields;

    private final int length;

    private final int segmentIndex;

    public SegmentDefinition(int segmentIndex, StringFieldDefinition segmentIndicatorField, List<SegmentFieldDefinition<?>> fields) {
      this.segmentIndex = segmentIndex;
      this.segmentIndicatorField = Objects.requireNonNull(segmentIndicatorField, "segment indicator field");
      this.fields = fields;
      this.length = computeLength(fields);
    }

    int getLength() {
      return this.length;
    }

    public StringFieldDefinition getSegmentIndicatorField() {
      return this.segmentIndicatorField;
    }

    int getFieldCount() {
      return this.fields.size();
    }

    public List<SegmentFieldDefinition<?>> getFields() {
      return this.fields;
    }

    @Override
    public String toString() {
      return "Segement(name=" + this.segmentIndicatorField.getName() + ", length" + this.length + ")";
    }

  }

}
