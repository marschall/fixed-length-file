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

  String getName() {
    return this.name;
  }

  @Override
  public boolean equals(Object obj) {
    // name is unique
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    return this.name.equals(((FieldDefinition) obj).name);
  }

  @Override
  public int hashCode() {
    return ((31 + this.name.hashCode()) * 31) + this.getClass().hashCode();
  }

  @Override
  public String toString() {
    return this.name;
  }

  public static final class StringFieldDefinition extends FieldDefinition {

    public StringFieldDefinition(String name, int length) {
      super(name, length);
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
