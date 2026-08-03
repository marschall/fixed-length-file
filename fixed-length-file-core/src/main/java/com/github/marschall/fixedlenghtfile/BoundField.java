package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

public abstract sealed class BoundField<D extends FieldDefinition> {
  
  final short offset;
  
  final D definition;
  
  BoundField(short offset, D definition) {
    this.offset = offset;
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

    BoundStringField(short offset, StringFieldDefinition definition) {
      super(offset, definition);
    }

  }

  public static final class BoundIntegerField extends BoundField<UnsignedFieldDefinition> {

    BoundIntegerField(short offset, UnsignedFieldDefinition definition) {
      super(offset, definition);
    }

  }

  public static final class BoundLongField extends BoundField<UnsignedFieldDefinition> {

    BoundLongField(short offset, UnsignedFieldDefinition definition) {
      super(offset, definition);
    }

  }

  public static final class BoundSegmentIndicatorField extends BoundField<StringFieldDefinition> {

    BoundSegmentIndicatorField(short offset, StringFieldDefinition definition) {
      super(offset, definition);
    }

  }


}
