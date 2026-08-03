package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

public final class FieldBinder {

  private int currentOffset;

  public FieldBinder() {
    this.currentOffset = 0;
  }

  public BoundStringField bind(StringFieldDefinition definition) {
    var result = new BoundStringField((short) this.currentOffset, definition);
    this.currentOffset += definition.getLength();
    return result;
  }

  public BoundIntegerField bind(UnsignedFieldDefinition definition) {
    var result = new BoundIntegerField((short) this.currentOffset, definition);
    this.currentOffset += definition.getLength();
    return result;
  }

  int getLength() {
    return this.currentOffset;
  }

}
