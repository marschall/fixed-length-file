package com.github.marschall.fixedlenghtfile;

import java.util.Objects;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalDateField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;

public abstract sealed class BoundField {

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

      BoundStringField(short offset, StringFieldDefinition definition) {
        super(offset, definition);
      }

    }

    public static final class BoundIntegerField extends OffsetField<UnsignedFieldDefinition> {

      BoundIntegerField(short offset, UnsignedFieldDefinition definition) {
        super(offset, definition);
      }

    }

    public static final class BoundLocalDateField extends OffsetField<UnsignedFieldDefinition> {

      BoundLocalDateField(short offset, UnsignedFieldDefinition definition) {
        if (definition.getLength() != 8) {
          throw new IllegalArgumentException("length of " + definition + " must be 8");
        }
        super(offset, definition);
      }

    }

    public static final class BoundLocalTimeField extends OffsetField<UnsignedFieldDefinition> {

      BoundLocalTimeField(short offset, UnsignedFieldDefinition definition) {
        if (definition.getLength() != 6 && definition.getLength() != 8) {
          throw new IllegalArgumentException("length of " + definition + " must be 6 or 8");
        }
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

  public static final class BoundLocalDateTimeField extends BoundField {

    private final BoundLocalDateField localDateField;
    private final BoundLocalTimeField localTimeField;

    BoundLocalDateTimeField(BoundLocalDateField localDateField, BoundLocalTimeField localTimeField) {
      this.localDateField = Objects.requireNonNull(localDateField, "localDateField");
      this.localTimeField = Objects.requireNonNull(localTimeField, "localTimeField");

    }

    BoundLocalDateField getLocalDateField() {
      return this.localDateField;
    }

    BoundLocalTimeField getLocalTimeField() {
      return this.localTimeField;
    }

  }
  
  public static final class BoundBigDecimalField extends BoundField {

    private final BoundLongField amountField;
    private final BoundIntegerField exponentField;

    BoundBigDecimalField(BoundLongField amountField, BoundIntegerField exponentField) {
      this.amountField = Objects.requireNonNull(amountField, "amountField");
      this.exponentField = Objects.requireNonNull(exponentField, "localTimeField");

    }

    BoundLongField getAmountField() {
      return this.amountField;
    }

    BoundIntegerField getExponentField() {
      return this.exponentField;
    }

  }

  public static final class BoundSegmentedField<F extends OffsetField<?>> extends BoundField {

    private final F delegate;
    private final BoundSegmentIndicatorField segmentIndicator;

    BoundSegmentedField(BoundSegmentIndicatorField segmentIndicator, F delegate) {
      this.segmentIndicator = Objects.requireNonNull(segmentIndicator, "segmentIndicator");
      this.delegate = Objects.requireNonNull(delegate, "delegate");
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
