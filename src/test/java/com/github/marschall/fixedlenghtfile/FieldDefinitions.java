package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

final class FieldDefinitions {

  private FieldDefinitions() {
    throw new AssertionError("not instantiable");
  }

  static final StringFieldDefinition FIELD1 = new StringFieldDefinition("FIELD-1", (short) 6);

  static final UnsignedFieldDefinition FIELD2 = new UnsignedFieldDefinition("FIELD-2", (short) 2);

}
