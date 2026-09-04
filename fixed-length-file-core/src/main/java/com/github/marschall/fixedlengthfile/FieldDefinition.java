package com.github.marschall.fixedlengthfile;

import java.util.Objects;

public abstract sealed class FieldDefinition {

  private final String name;

  FieldDefinition(String name) {
    this.name = Objects.requireNonNull(name, "name");
  }

  public String getName() {
    return this.name;
  }

  abstract int getLength();

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

  public static abstract sealed class OffsetFieldDefinition extends FieldDefinition {

    private final short length;

    private final short offset;

    OffsetFieldDefinition(String name, int length, int offset) {
      super(name);
      if (length <= 0) {
        throw new IllegalArgumentException("length must be positive");
      }
      this.length = Utils.toPositiveShortExact(length);
      this.offset = Utils.toPositiveShortExact(offset);
    }

    public int getLength() {
      return this.length;
    }

    int getOffset() {
      return this.offset;
    }

    public static final class StringFieldDefinition extends OffsetFieldDefinition {

      public StringFieldDefinition(String name, int length, int offset) {
        super(name, length, offset);
      }

    }

    public static final class UnsignedFieldDefinition extends OffsetFieldDefinition {

      public UnsignedFieldDefinition(String name, int length, int offset) {
        super(name, length, offset);
      }

    }

    public static final class SignedFieldDefinition extends OffsetFieldDefinition {

      public SignedFieldDefinition(String name, int length, int offset) {
        super(name, length, offset);
      }

    }
  }

  public static final class SegmentFieldDefinition<F extends OffsetFieldDefinition> extends FieldDefinition {

    private final F delegate;
    private final short segmentIndex;

    public SegmentFieldDefinition(int segmentIndex, F delegate) {
      super(delegate.getName());
      this.delegate = delegate;
      this.segmentIndex = Utils.toPositiveShortExact(segmentIndex);
    }

    public int getSegmentIndex() {
      return this.segmentIndex;
    }

    @Override
    int getLength() {
      return this.delegate.getLength();
    }

    public F getDelegate() {
      return this.delegate;
    }

  }


}
