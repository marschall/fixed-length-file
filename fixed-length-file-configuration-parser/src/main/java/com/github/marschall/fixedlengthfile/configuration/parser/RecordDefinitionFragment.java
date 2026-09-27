package com.github.marschall.fixedlengthfile.configuration.parser;

import java.util.List;

public abstract sealed class RecordDefinitionFragment {

  
  private final List<Field> fields;

  RecordDefinitionFragment(List<Field> fields) {
    this.fields = fields;
  }
  
  public List<Field> getFields() {
    return this.fields;
  }
  
  public int getLengthOfFields() {
    int size = 0;
    for (Field field : fields) {
      size += field.length();
    }
    return size;
  }

  static final class RecordDefinition extends RecordDefinitionFragment {

    private final String name;
    private final List<SegmentDefinition> segments;

    RecordDefinition(String name, List<Field> fields, List<SegmentDefinition> segments) {
      super(fields);
      this.name = name;
      this.segments = segments;
    }

    public String getName() {
      return this.name;
    }

    public List<SegmentDefinition> getSegments() {
      return this.segments;
    }
    
    public boolean hasSegments() {
      return !this.segments.isEmpty();
    }
    
    @Override
    public String toString() {
      return this.name;
    }

  }

  static final class SegmentDefinition extends RecordDefinitionFragment {

    private final int segmentIndex;

    SegmentDefinition(int segmentIndex, List<Field> fields) {
      super(fields);
      this.segmentIndex = segmentIndex;
    }

    public int getSegmentIndex() {
      return this.segmentIndex;
    }

  }

}
