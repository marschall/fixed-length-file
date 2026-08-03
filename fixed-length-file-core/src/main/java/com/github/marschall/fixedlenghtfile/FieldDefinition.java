package com.github.marschall.fixedlenghtfile;

import java.util.Objects;

public abstract sealed class FieldDefinition {

  private final String name;

  private final short length;

  FieldDefinition(String name, int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("length must be positive");
    }
    this.name = Objects.requireNonNull(name, "name");
    this.length = Utils.toPositiveShortExact(length);
  }

  int getLength() {
    return length;
  }

  public static final class StringFieldDefinition extends FieldDefinition {

    public StringFieldDefinition(String name, int length) {
      super(name, length);
    }

  }

  public static final class SegmentFieldDefinition extends FieldDefinition {

    public SegmentFieldDefinition(String name) {
      super(name, 1);
    }

  }

  public static final class UnsignedFieldDefinition extends FieldDefinition {

    public UnsignedFieldDefinition(String name, int length) {
      super(name, length);
    }

  }

  public static final class SignedFieldDefinition extends FieldDefinition {

    public SignedFieldDefinition(String name, int length) {
      super(name, length);
    }

  }

}
