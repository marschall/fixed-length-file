package com.github.marschall.fixedlenghtfile;

import java.util.Objects;

public abstract sealed class FieldDefinition {

  private final String name;

  private final short length;

  FieldDefinition(String name, short length) {
    if (length <= 0) {
      throw new IllegalArgumentException("length must be positive");
    }
    this.name = Objects.requireNonNull(name, "name");
    this.length = length;
  }
  
  int getLength() {
    return length;
  }

  public static final class StringFieldDefinition extends FieldDefinition {

    public StringFieldDefinition(String name, short length) {
      super(name, length);
    }

  }

  public static final class UnsignedFieldDefinition extends FieldDefinition {

    public UnsignedFieldDefinition(String name, short length) {
      super(name, length);
    }

  }

  public static final class SignedFieldDefinition extends FieldDefinition {

    public SignedFieldDefinition(String name, short length) {
      super(name, length);
    }

  }

}
