package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

public abstract sealed class BoundField<D extends FieldDefinition> {
  
  final short offset;
  
  final D definition;
  
  BoundField(int offset, D definition) {
    this.offset = Utils.toPositiveShortExact(offset);
    this.definition = definition;
  }
  
  int getOffset() {
    return this.offset;
  }

  int getLength() {
    return this.definition.getLength();
  }

  public static final class BoundStringField extends BoundField<StringFieldDefinition> {
    // TODO single char
    // TODO date
    // TODO time

    BoundStringField(int offset, StringFieldDefinition definition) {
      super(offset, definition);
    }

  }

  public static final class BoundIntegerField extends BoundField<UnsignedFieldDefinition> {

    BoundIntegerField(int offset, UnsignedFieldDefinition definition) {
      super(offset, definition);
    }

  }

  public static final class BoundLongField extends BoundField<UnsignedFieldDefinition> {

    BoundLongField(int offset, UnsignedFieldDefinition definition) {
      super(offset, definition);
    }

  }

  public static final class BoundSegmentIndicatorField extends BoundField<SegmentFieldDefinition> {

    BoundSegmentIndicatorField(int offset, SegmentFieldDefinition definition) {
      super(offset, definition);
    }

  }


}
