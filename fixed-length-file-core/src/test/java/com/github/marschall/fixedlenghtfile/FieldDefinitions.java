package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

final class FieldDefinitions {

  private FieldDefinitions() {
    throw new AssertionError("not instantiable");
  }

  static final StringFieldDefinition TYPE = new StringFieldDefinition("TYPE", 1);

  static final StringFieldDefinition FIELD1 = new StringFieldDefinition("FIELD-1", 6);
  
  static final StringFieldDefinition FIELD2 = new StringFieldDefinition("FIELD-2", 6);

  static final StringFieldDefinition FIELD3 = new StringFieldDefinition("FIELD-3", 6);
  
  static final StringFieldDefinition FIELD4 = new StringFieldDefinition("FIELD-4", 6);
  
  static final StringFieldDefinition FIELD5 = new StringFieldDefinition("FIELD-5", 6);

  static final UnsignedFieldDefinition FIELD6 = new UnsignedFieldDefinition("FIELD-6", 2);
  
  static final UnsignedFieldDefinition FIELD7 = new UnsignedFieldDefinition("FIELD-7", 2);
  
  static final UnsignedFieldDefinition DATE_FIELD = new UnsignedFieldDefinition("DATE-FIELD", 8);
  
  static final UnsignedFieldDefinition TIME_FIELD6 = new UnsignedFieldDefinition("TIME-FIELD-6", 6);
  
  static final UnsignedFieldDefinition TIME_FIELD8 = new UnsignedFieldDefinition("TIME-FIELD-8", 8);
  
  static final UnsignedFieldDefinition AMOUNT_FIELD = new UnsignedFieldDefinition("AMOUNT-FIELD", 12);
  
  static final UnsignedFieldDefinition EXPONENT_FIELD = new UnsignedFieldDefinition("EXPONENT-FIELD", 1);

}
