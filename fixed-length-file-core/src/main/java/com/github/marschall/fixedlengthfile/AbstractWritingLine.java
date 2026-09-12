package com.github.marschall.fixedlengthfile;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.marschall.fixedlengthfile.AbstractWritingLine.Initializer.AbstractOffsetLengthInitializer.CharInitializer;
import com.github.marschall.fixedlengthfile.AbstractWritingLine.Initializer.AbstractOffsetLengthInitializer.NumInitializer;
import com.github.marschall.fixedlengthfile.AbstractWritingLine.Initializer.CompositeInitializer;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;

abstract class AbstractWritingLine implements WritingLine {
  
  // abstract methods

  abstract void writePaddingNumber(int offset, int padding);

  abstract void writeCharAt(int index, char c);

  abstract void writePaddingString(int offset, int padding);

  // utility methods

  static int digits(int i) {
    if (i < 0) {
      throw new IllegalArgumentException("value must be positive");
    }
    int p = 10;
    for (int j = 1; j < 10; j++) {
      if (i < p) {
        return j;
      }
      p = 10 * p;
    }
    return 10;
  }

  static int digits(long l) {
    if (l < 0) {
      throw new IllegalArgumentException("value must be positive");
    }
    long p = 10L;
    for (int j = 1; j < 19; j++) {
      if (l < p) {
        return j;
      }
      p = 10 * p;
    }
    return 19;
  }
  
  // intialization

  void initializeFor(FixedLengthRecordDefinition recordDefinition) {
    // TODO cache
    Initializer initializer = buildInitializer(recordDefinition);
    initializer.initialize(this);
    
    // first field is record type
    StringFieldDefinition recordDefinitionField = (StringFieldDefinition) recordDefinition.getFields().getFirst();
    this.writeString(recordDefinitionField, recordDefinition.getType());
  }
  
  static Initializer buildInitializer(FixedLengthRecordDefinition recordDefinition) {
    List<? extends OffsetFieldDefinition> allFields = recordDefinition.getFields();
    // the first field is the record type, this has to be set always
    return buildInitializer(allFields.subList(1, allFields.size()));
  }

  private static Initializer buildInitializer(List<? extends OffsetFieldDefinition> fields) {
    List<Initializer> initializers = new ArrayList<>();
    OffsetFieldDefinition firstField = fields.getFirst();
    int currentOffset = firstField.getOffset();
    int currentLength = firstField.getLength();
    FieldType previousType = getType(firstField);
    for (OffsetFieldDefinition fieldDefinition : fields.subList(1, fields.size())) {
      FieldType currentType = getType(fieldDefinition);
      if (currentType == previousType) {
        currentLength += fieldDefinition.getLength();
      } else {
        initializers.add(instantiateInitializer(previousType, currentOffset, currentLength));

        currentOffset = fieldDefinition.getOffset();
        currentLength = fieldDefinition.getLength();
        previousType = currentType;
      }
    }
    initializers.add(instantiateInitializer(previousType, currentOffset, currentLength));
    return new CompositeInitializer(initializers);
  }
  
  private static Initializer instantiateInitializer(FieldType type, int offset, int length) {
    return switch(type) {
      case CHAR -> new CharInitializer(offset, length);
      case NUM -> new NumInitializer(offset, length);
    };
  }

  private static FieldType getType(OffsetFieldDefinition fieldDefinition) {
    return switch (fieldDefinition) {
      case StringFieldDefinition _ -> FieldType.CHAR;
      case UnsignedFieldDefinition _ -> FieldType.NUM;
      default -> throw new IllegalArgumentException("Unexpected value: " + fieldDefinition);
    };
  }

  enum FieldType {

    CHAR,
    NUM;

  }
  
  sealed interface Initializer {

    void initialize(AbstractWritingLine line);

    static final class CompositeInitializer implements Initializer {

      private final List<Initializer> initializers;

      CompositeInitializer(List<Initializer> initializers) {
        this.initializers = Objects.requireNonNull(initializers, "initializers");

      }

      @Override
      public void initialize(AbstractWritingLine line) {
        for (Initializer initializer : this.initializers) {
          initializer.initialize(line);
        }

      }

    }

    abstract sealed static class AbstractOffsetLengthInitializer implements Initializer {

      private final short offset;

      private final short length;

      protected AbstractOffsetLengthInitializer(int offset, int length) {
        if (offset < 0 || offset > Short.MAX_VALUE) {
          throw new IllegalArgumentException();
        }
        if (length < 0 || length > Short.MAX_VALUE) {
          throw new IllegalArgumentException();
        }
        this.offset = (short) offset;
        this.length = (short) length;
      }

      protected int getOffset() {
        return this.offset;
      }

      protected int getLength() {
        return this.length;
      }

      static final class CharInitializer extends AbstractOffsetLengthInitializer {

        CharInitializer(int offset, int length) {
          super(offset, length);
        }

        @Override
        public void initialize(AbstractWritingLine line) {
          line.writePaddingString(this.getOffset(), this.getLength());
        }

      }

      static final class NumInitializer extends AbstractOffsetLengthInitializer {

        NumInitializer(int offset, int length) {
          super(offset, length);
        }

        @Override
        public void initialize(AbstractWritingLine line) {
          line.writePaddingNumber(this.getOffset(), this.getLength());
        }

      }

    }

  }

  // public methods

  @Override
  public void writeUnsignedInt(UnsignedFieldDefinition field, int value) {
    int offset = field.getOffset();
    int length = field.getLength();
    int digits = digits(value);
    int padding = length - digits;
    this.writePaddingNumber(offset, padding);
    int remaining = value;
    for (int i = 0; i < digits; i++) {
      int digit = remaining % 10;
      writeCharAt(offset + length - i - 1, (char) ('0' + digit));
      remaining = remaining / 10;
    }
  }

  @Override
  public void writeNoValue(UnsignedFieldDefinition field) {
    int offset = field.getOffset();
    int length = field.getLength();
    this.writePaddingNumber(offset, length);
  }

  @Override
  public void writeUnsignedLong(UnsignedFieldDefinition field, long value) {
    int offset = field.getOffset();
    int length = field.getLength();
    int digits = digits(value);
    int padding = length - digits;
    writePaddingNumber(offset, padding);
    long remaining = value;
    for (int i = 0; i < digits; i++) {
      int digit = (int) (remaining % 10L);
      writeCharAt(offset + length - i - 1, (char) ('0' + digit));
      remaining = remaining / 10L;
    }
  }

  @Override
  public void writeString(StringFieldDefinition field, String s) {
    int offset = field.getOffset();
    int length = field.getLength();
    if (s != null) {
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c > 255) {
          throw new IllegalArgumentException("non-latin 1 character encountered");
        }
        writeCharAt(offset + i, c);
      }
    }
    int stringLength = s != null ? s.length() : 0;
    int padding = length - stringLength;
    this.writePaddingString(offset + stringLength, padding);
  }

  @Override
  public void writeSegmentIndicator(StringFieldDefinition field, SegmentIndicator indicator) {
    int offset = field.getOffset();
    char c = indicator.getValue();
    writeCharAt(offset, c);
  }

  @Override
  public void writeNoValue(StringFieldDefinition field) {
    int offset = field.getOffset();
    int length = field.getLength();
    this.writePaddingString(offset, length);
  }
  
  // high level public methods

  @Override
  public void writeLocalDate(UnsignedFieldDefinition field, LocalDate value) {
    int yyyyMMdd = value.getYear() * 10000
        + value.getMonthValue() * 100
        + value.getDayOfMonth();
    writeUnsignedInt(field, yyyyMMdd);
  }

  @Override
  public void writeLocalTime(UnsignedFieldDefinition field, LocalTime value) {
    int hhmmss = value.getHour() * 10000
        + value.getMinute() * 100
        + value.getMinute();
    if (field.getLength() == 8) {
      writeUnsignedInt(field, hhmmss * 100 + value.getNano() / 10_000_00);
    } else {
      writeUnsignedInt(field, hhmmss);
    }
  }

  @Override
  public void writeLocalDateTime(UnsignedFieldDefinition dateField, UnsignedFieldDefinition timeField, LocalDateTime value) {
    writeLocalDate(dateField, value.toLocalDate());
    writeLocalTime(timeField, value.toLocalTime());
  }

  @Override
  public void writeBigDecimal(UnsignedFieldDefinition amountField, UnsignedFieldDefinition exponentField,
      BigDecimal value, int scale) {
    // option value.movePointRight(value.scale());
    BigInteger unscaledValue = value.unscaledValue();
    if (amountField.getLength() <= 9) {
      writeUnsignedInt(amountField, unscaledValue.intValueExact());
    } else {
      writeUnsignedLong(amountField, unscaledValue.longValueExact());
    }
    writeUnsignedInt(exponentField, scale);
  }

}
