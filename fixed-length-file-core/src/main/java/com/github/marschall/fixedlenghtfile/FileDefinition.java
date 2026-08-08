package com.github.marschall.fixedlenghtfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;

public final class FileDefinition {

  private final List<RecordDefinition> recordDefinitions;
  private final Map<String, RecordDefinition> recordDefinitionMap;

  public FileDefinition(List<RecordDefinition> recordDefinitions) {
    Objects.requireNonNull(recordDefinitions, "recordDefinitions");
    this.recordDefinitions = recordDefinitions;
    this.recordDefinitionMap = buildRecordDefinitionMap(recordDefinitions);
  }

  private static Map<String, RecordDefinition> buildRecordDefinitionMap(List<RecordDefinition> recordDefinitions) {
    Map<String, RecordDefinition> map = HashMap.newHashMap(recordDefinitions.size());
    for (RecordDefinition recordDefinition : recordDefinitions) {
      map.put(recordDefinition.getType(), recordDefinition);
    }
    return map;
  }
  
  Map<String, RecordDefinition> getRecordDefinitionMap() {
    return this.recordDefinitionMap;
  }
  
  public RecordDefinition getRecordDefinition(String recordType) {
    return this.recordDefinitionMap.get(recordType);
  }

  int getMaximumPrefixLength() {
    int maximum = 0;
    for (String prefix : this.recordDefinitionMap.keySet()) {
      maximum = Math.max(maximum, prefix.length());
    }
    return maximum;
  }

  public static FileDefinitionBuilder builder() {
    return new InternalFileDefinitionBuilder();
  }
  
  static final class InternalFileDefinitionBuilder implements FileDefinitionBuilder {

    private List<RecordDefinition> recordDefinitions;

    InternalFileDefinitionBuilder() {
      this.recordDefinitions = new ArrayList<>();
    }

    @Override
    public FileDefinitionBuilder defineRecordType(String recordType, Consumer<FixedPartFieldBinder> binderConsumer) {
      Objects.requireNonNull(recordType, "recordType");
      InternalFixedPartFieldBinder lineFieldBinder = new InternalFixedPartFieldBinder(recordType);
      binderConsumer.accept(lineFieldBinder);
      this.recordDefinitions.add(lineFieldBinder.build());
      return this;
    }

    @Override
    public FileDefinition build() {
      return new FileDefinition(this.recordDefinitions);
    }

  }

  record FieldAndOffset(short offset, FieldDefinition definition) {

    int offsetAsInt() {
      return Short.toUnsignedInt(this.offset);
    }

    int length() {
      return this.definition.getLength();
    }

  }
  
  static abstract class InternalLineFieldBinder implements LineFieldBinder {

    private int currentOffset;

    protected List<FieldAndOffset> fields;

    InternalLineFieldBinder() {
      this.currentOffset = 0;
      this.fields = new ArrayList<>();
    }

    @Override
    public void bind(FieldDefinition definition) {
      this.fields.add(new FieldAndOffset(Utils.toPositiveShortExact(this.currentOffset), definition));
      this.currentOffset += definition.getLength();
    }
  }
  
  static final class InternalFixedPartFieldBinder extends InternalLineFieldBinder implements FixedPartFieldBinder {

    private final String recordType;
    private final List<SegmentDefinition> segments;

    InternalFixedPartFieldBinder(String recordType) {
      this.recordType = recordType;
      this.segments = new ArrayList<>();
    }

    @Override
    public void defineSegment(StringFieldDefinition segmentIndicatorField, Consumer<SegmentFieldBinder> binderConsumer) {
      this.bind(segmentIndicatorField);
      InternalSegmentFieldBinder segmentFieldBinder = new InternalSegmentFieldBinder(this.segments.size(), segmentIndicatorField);
      binderConsumer.accept(segmentFieldBinder);
      this.segments.add(segmentFieldBinder.build());
    }
    
    RecordDefinition build() {
      if (this.segments.isEmpty()) {
        return new FixedLengthRecordDefinition(this.recordType, this.fields);
      } else {
        return new SegmentedRecordDefinition(this.recordType, this.fields, this.segments);
      }
    }
    
  }

  static final class InternalSegmentFieldBinder extends InternalLineFieldBinder implements SegmentFieldBinder {

    private final StringFieldDefinition segmentIndicatorField;
    private final int segmentIndex;

    InternalSegmentFieldBinder(int segmentIndex, StringFieldDefinition segmentIndicatorField) {
      this.segmentIndex = segmentIndex;
      this.segmentIndicatorField = segmentIndicatorField;
    }

    SegmentDefinition build() {
      return new SegmentDefinition(this.segmentIndex, this.segmentIndicatorField, this.fields);
    }

  }

}
