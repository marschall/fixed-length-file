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

  public FileDefinition(List<RecordDefinition> recordDefinitions) {
    this.recordDefinitions = recordDefinitions;
  }

  public Map<String, RecordDefinition> getRecordDefinitionMap() {
    Map<String, RecordDefinition> map = HashMap.newHashMap(this.recordDefinitions.size());
    for (RecordDefinition recordDefinition : this.recordDefinitions) {
      map.put(recordDefinition.getType(), recordDefinition);
    }
    return map;
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
      InternalSegmentFieldBinder segmentFieldBinder = new InternalSegmentFieldBinder();
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
    
    SegmentDefinition build() {
      // FIXME
      return null;
    }
    
  }

}
