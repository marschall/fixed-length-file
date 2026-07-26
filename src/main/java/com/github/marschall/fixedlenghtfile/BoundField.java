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

  public static final class BoundStringField extends BoundField<StringFieldDefinition> {
    // TODO single char
    // TODO date
    // TODO time

    public BoundStringField(short offset, StringFieldDefinition definition) {
      super(offset, definition);
    }

    String readTrimmedStringAt(ReadingLine line) {
      return line.readTrimmedStringAt(this.offset, this.definition.getLength());
    }

  }

  public static final class UnsignedIntegerFieldDefinition extends BoundField<UnsignedFieldDefinition> {

    public UnsignedIntegerFieldDefinition(short offset, UnsignedFieldDefinition definition) {
      super(offset, definition);
    }

    int readUnsignedIntAt(ReadingLine line) {
      return line.readUnsignedIntAt(this.offset, this.definition.getLength());
    }

  }

}
