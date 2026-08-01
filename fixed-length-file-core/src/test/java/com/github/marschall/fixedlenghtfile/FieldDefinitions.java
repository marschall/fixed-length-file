package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

final class FieldDefinitions {

  private FieldDefinitions() {
    throw new AssertionError("not instantiable");
  }

  static final StringFieldDefinition FIELD1 = new StringFieldDefinition("FIELD-1", (short) 6);
  
  static final StringFieldDefinition FIELD2 = new StringFieldDefinition("FIELD-2", (short) 6);

  static final StringFieldDefinition FIELD3 = new StringFieldDefinition("FIELD-3", (short) 6);
  
  static final StringFieldDefinition FIELD4 = new StringFieldDefinition("FIELD-4", (short) 6);
  
  static final StringFieldDefinition FIELD5 = new StringFieldDefinition("FIELD-5", (short) 6);

  static final UnsignedFieldDefinition FIELD6 = new UnsignedFieldDefinition("FIELD-6", (short) 2);
  
  static final UnsignedFieldDefinition FIELD7 = new UnsignedFieldDefinition("FIELD-7", (short) 2);

}
