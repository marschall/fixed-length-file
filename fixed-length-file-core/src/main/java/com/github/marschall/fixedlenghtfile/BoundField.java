package com.github.marschall.fixedlenghtfile;

import java.util.Objects;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

public abstract sealed class BoundField {

  abstract int getOffset();

  abstract int getLength();

  abstract static sealed class OffsetField<D extends FieldDefinition> extends BoundField {

    final short offset;

    final D definition;

    OffsetField(short offset, D definition) {
      this.offset = offset;
      this.definition = Objects.requireNonNull(definition, "defintion");
    }

    int getOffset() {
      return this.offset;
    }

    int getLength() {
      return this.definition.getLength();
    }
    
    @Override
    public String toString() {
      return this.definition.toString();
    }

    public static final class BoundStringField extends OffsetField<StringFieldDefinition> {
      // TODO single char
      // TODO date
      // TODO time

      BoundStringField(short offset, StringFieldDefinition definition) {
        super(offset, definition);
      }

    }

    public static final class BoundIntegerField extends OffsetField<UnsignedFieldDefinition> {

      BoundIntegerField(short offset, UnsignedFieldDefinition definition) {
        super(offset, definition);
      }

    }

    public static final class BoundLongField extends OffsetField<UnsignedFieldDefinition> {

      BoundLongField(short offset, UnsignedFieldDefinition definition) {
        super(offset, definition);
      }

    }

    public static final class BoundSegmentIndicatorField extends OffsetField<StringFieldDefinition> {

      BoundSegmentIndicatorField(short offset, StringFieldDefinition definition) {
        super(offset, definition);
      }

    }

  }

  public static final class BoundSegmentedField<F extends OffsetField<?>> extends BoundField {

    private final F delegate;
    private final BoundSegmentIndicatorField segmentIndicator;

    BoundSegmentedField(BoundSegmentIndicatorField segmentIndicator, F delegate) {
      this.segmentIndicator = Objects.requireNonNull(segmentIndicator, "segmentIndicator");
      this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    int getOffset() {
      return this.delegate.getOffset();
    }

    @Override
    int getLength() {
      return this.delegate.getLength();
    }

    F getUnderlyingField() {
      return this.delegate;
    }

    BoundSegmentIndicatorField getSegmentIndicator() {
      return this.segmentIndicator;
    }

    @Override
    public String toString() {
      return this.delegate + " in: " + this.segmentIndicator;
    }

  }

}
