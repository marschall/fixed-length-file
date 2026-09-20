package com.github.marschall.fixedlengthfile;

import java.math.BigDecimal;
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
import com.github.marschall.fixedlengthfile.FieldDefinition.SegmentFieldDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;
import com.github.marschall.fixedlengthfile.SegmentOffsets.ArrayBasedSegmentOffsets;

abstract class AbstractWritingLine implements WritingLine {
  
  private SegmentOffsets segmentOffsets;
  
  AbstractWritingLine() {
    this.segmentOffsets = SegmentOffsets.NoSegment.INSTANCE;
  }
  
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
  void initializeFor(SegmentedRecordDefinition recordDefinition, List<SegmentIndicator> segmentIndicators) {
    List<SegmentDefinition> segmentDefinitions = recordDefinition.getSegmentDefinitions();
    if (segmentDefinitions.size() != segmentIndicators.size()) {
      throw new IllegalArgumentException("segment indicator list mismatch");
    }
    // TODO cache
    this.segmentOffsets = createSegmentOffsets(recordDefinition, segmentIndicators);
    int recordLength = recordDefinition.computeRecordLength(this.segmentOffsets);
    this.doSetLength(recordLength);
    
    // first field is record type
    StringFieldDefinition recordDefinitionField = (StringFieldDefinition) recordDefinition.getFields().getFirst();
    this.writeString(recordDefinitionField, recordDefinition.getType());
    
    // initialize all the segment indicators
    for (int i = 0; i < segmentDefinitions.size(); i++) {
      StringFieldDefinition segmentIndicatorField = segmentDefinitions.get(i).getSegmentIndicatorField();
      var segmentIndicator = segmentIndicators.get(i);
      this.writeSegmentIndicator(segmentIndicatorField, segmentIndicator);
    }
    
    
    // TODO cache
    Initializer baseInitializer = buildRecordInitializer(recordDefinition);
    List<SegmentInitializer> segmentInitializers = new ArrayList<>(segmentDefinitions.size());
    for (SegmentDefinition segmentDefinition : segmentDefinitions) {
      segmentInitializers.add(buildSegmentInitializer(segmentDefinition.getFields()));
    }
    SegmentedRecordInitializer recordInitializer = new SegmentedRecordInitializer(baseInitializer, segmentInitializers);
    recordInitializer.initialize(this, recordDefinitionField.getLength(), segmentIndicators);
  }

  protected abstract void doSetLength(int recordLength);

  private SegmentOffsets createSegmentOffsets(SegmentedRecordDefinition recordDefinition, List<SegmentIndicator> segmentIndicators) {
    List<SegmentDefinition> segmentDefinitions = recordDefinition.getSegmentDefinitions();
    int offset = recordDefinition.getBaseLength();
    var segmentOffsets = new ArrayBasedSegmentOffsets(segmentDefinitions.size());
    for (int i = 0; i < segmentDefinitions.size(); i++) {
      var segmentDefinition = segmentDefinitions.get(i);
      var segmentIndicator = segmentIndicators.get(i);
      switch (segmentIndicator) {
        case PRESENT -> {
          segmentOffsets.setSegmentOffset(i, offset);
          offset += segmentDefinition.getLength();
        }
        case SPACES -> {
          segmentOffsets.setSegmentIsSpaces(i);
          offset += segmentDefinition.getLength();
        }
        case ABSENT -> {
          segmentOffsets.setSegmentNotPresent(i);
        }
      };

    }
    return segmentOffsets;
  }

  private static SegmentInitializer buildSegmentInitializer(List<SegmentFieldDefinition<?>> fields) {
    List<? extends OffsetFieldDefinition> offsetFields = new MappedList<>(fields, SegmentFieldDefinition::getDelegate);
    Initializer fieldInitializer = buildFieldInitializer(offsetFields);
    return new SegmentInitializer(fieldInitializer);
  }

  void initializeFor(FixedLengthRecordDefinition recordDefinition) {
    // first field is record type
    StringFieldDefinition recordDefinitionField = (StringFieldDefinition) recordDefinition.getFields().getFirst();
    this.doSetLength(recordDefinition.getMaximumLength());
    this.writeString(recordDefinitionField, recordDefinition.getType());

    // TODO cache
    Initializer initializer = buildRecordInitializer(recordDefinition);
    initializer.initialize(this, recordDefinitionField.getLength());
  }

  static Initializer buildRecordInitializer(RecordDefinition recordDefinition) {
    List<? extends OffsetFieldDefinition> allFields = recordDefinition.getFields();
    // the first field is the record type, this has to be set always
    return buildFieldInitializer(allFields.subList(1, allFields.size()));
  }

  private static Initializer buildFieldInitializer(List<? extends OffsetFieldDefinition> fields) {
    List<Initializer> initializers = new ArrayList<>();
    OffsetFieldDefinition firstField = fields.getFirst();
    int currentLength = firstField.getLength();
    FieldType previousType = getType(firstField);
    for (OffsetFieldDefinition fieldDefinition : fields.subList(1, fields.size())) {
      FieldType currentType = getType(fieldDefinition);
      if (currentType == previousType) {
        currentLength += fieldDefinition.getLength();
      } else {
        initializers.add(instantiateInitializer(previousType, currentLength));

        currentLength = fieldDefinition.getLength();
        previousType = currentType;
      }
    }
    initializers.add(instantiateInitializer(previousType, currentLength));
    return new CompositeInitializer(initializers);
  }

  private static Initializer instantiateInitializer(FieldType type, int length) {
    return switch(type) {
      case CHAR -> new CharInitializer(length);
      case NUM -> new NumInitializer(length);
    };
  }
  
  private static FieldType getType(SegmentFieldDefinition<?> fieldDefinition) {
    return getType(fieldDefinition.getDelegate());
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

  static final class SegmentedRecordInitializer {

    private final Initializer baseInitializer;
    private final List<SegmentInitializer> segmentInitializers;

    SegmentedRecordInitializer(Initializer baseInitializer, List<SegmentInitializer> segmentInitializers) {
      this.baseInitializer = Objects.requireNonNull(baseInitializer, "baseInitializer");
      this.segmentInitializers = Objects.requireNonNull(segmentInitializers, "segmentInitializers");
    }

    int initialize(AbstractWritingLine line, int initialOffset, List<SegmentIndicator> segmentIndicators) {
      if (segmentIndicators.size() != this.segmentInitializers.size()) {
        throw new IllegalArgumentException("mismatched segment indicator size");
      }
      int offset = initialOffset;
      offset += this.baseInitializer.initialize(line, offset);
      for (int i = 0; i < segmentIndicators.size(); i++) {
        var segmentIndicator = segmentIndicators.get(i);
        var segmentInitializer = this.segmentInitializers.get(i);
        offset += segmentInitializer.initialize(line, offset, segmentIndicator);
      }
      return offset - initialOffset;
    }

  }

  static final class SegmentInitializer {

    private final Initializer delegate;

    SegmentInitializer(Initializer delegate) {
      this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    int initialize(AbstractWritingLine line, int offset, SegmentIndicator segmentIndicator) {
      if (segmentIndicator == SegmentIndicator.ABSENT) {
        return offset;
      }
      return this.delegate.initialize(line, offset);
    }

  }

  sealed interface Initializer {

    int initialize(AbstractWritingLine line, int offset);

    static final class CompositeInitializer implements Initializer {

      private final List<Initializer> initializers;

      CompositeInitializer(List<Initializer> initializers) {
        this.initializers = Objects.requireNonNull(initializers, "segmentInitializers");
      }

      @Override
      public int initialize(AbstractWritingLine line, int initalOffset) {
        int offset = initalOffset;
        for (Initializer initializer : this.initializers) {
          offset += initializer.initialize(line, offset);
        }
        return initalOffset - offset;
      }

    }

    abstract sealed class AbstractTangoInitializer implements Initializer {

      private final short[] fieldLengths;

      AbstractTangoInitializer(short[] fieldLengths) {
        this.fieldLengths = Objects.requireNonNull(fieldLengths, "fieldLengths");
      }

      @Override
      public int initialize(AbstractWritingLine line, int initialOffset) {
        int offset = initialOffset;
        FieldType fieldType = this.getInitialType();
        for (short length : fieldLengths) {
          switch (fieldType) {
            case NUM -> line.writePaddingNumber(offset, length);
            case CHAR -> line.writePaddingString(offset, length);
          };
          offset += length;
          fieldType = invert(fieldType);
        }
        return offset - initialOffset;
      }

      private static FieldType invert(FieldType fieldType) {
        return switch(fieldType) {
          case NUM -> FieldType.CHAR;
          case CHAR -> FieldType.NUM;
        };
      }

      abstract FieldType getInitialType();

      final class CharTangoInitializer extends AbstractTangoInitializer {

        CharTangoInitializer(short[] fieldLengths) {
          super(fieldLengths);
        }

        @Override
        FieldType getInitialType() {
          return FieldType.CHAR;
        }

      }

      final class NumTangoInitializer extends AbstractTangoInitializer {

        NumTangoInitializer(short[] fieldLengths) {
          super(fieldLengths);
        }

        @Override
        FieldType getInitialType() {
          return FieldType.NUM;
        }

      }

    }

    abstract sealed static class AbstractOffsetLengthInitializer implements Initializer {

      private final short length;

      protected AbstractOffsetLengthInitializer(int length) {
        if (length < 0 || length > Short.MAX_VALUE) {
          throw new IllegalArgumentException();
        }
        this.length = (short) length;
      }

      protected int getLength() {
        return this.length;
      }

      static final class CharInitializer extends AbstractOffsetLengthInitializer {

        CharInitializer(int length) {
          super(length);
        }
        
        @Override
        public int initialize(AbstractWritingLine line, int offset) {
          line.writePaddingString(offset, this.getLength());
          return this.getLength();
        }

      }

      static final class NumInitializer extends AbstractOffsetLengthInitializer {

        NumInitializer(int length) {
          super(length);
        }
        
        @Override
        public int initialize(AbstractWritingLine line, int offset) {
          line.writePaddingNumber(offset, this.getLength());
          return this.getLength();
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
      int hhmmsscc = hhmmss * 100 + value.getNano() / 10_000_00;
      writeUnsignedInt(field, hhmmsscc);
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
    BigDecimal fieldValue = value.movePointRight(scale);
    if (amountField.getLength() <= 9) {
      writeUnsignedInt(amountField, fieldValue.intValueExact());
    } else {
      writeUnsignedLong(amountField, fieldValue.longValueExact());
    }
    writeUnsignedInt(exponentField, scale);
  }

}
