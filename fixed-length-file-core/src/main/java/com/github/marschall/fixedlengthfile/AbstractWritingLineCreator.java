package com.github.marschall.fixedlengthfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.SegmentFieldDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;
import com.github.marschall.fixedlengthfile.SegmentOffsets.ArrayBasedSegmentOffsets;
import com.github.marschall.fixedlengthfile.SegmentOffsets.NoSegment;
import com.github.marschall.fixedlengthfile.AbstractWritingLineCreator.Initializer.AbstractTangoInitializer.CharTangoInitializer;
import com.github.marschall.fixedlengthfile.AbstractWritingLineCreator.Initializer.AbstractTangoInitializer.NumTangoInitializer;

public abstract class AbstractWritingLineCreator<W extends AbstractWritingLine> {

  private SegmentedRecordDefinition lastRecordDefinition;
  private List<SegmentIndicator> lastSegmentIndicators;
  private SegmentOffsets lastSegmentOffsets;
  private SegmentedRecordInitializer lastSegmentedRecordInitializer;

  AbstractWritingLineCreator() {
    this.lastRecordDefinition = null;
    this.lastSegmentIndicators = null;
    this.lastSegmentOffsets = null;
    this.lastSegmentedRecordInitializer = null;
  }

  public W writingLineFor(SegmentedRecordDefinition recordDefinition, List<SegmentIndicator> segmentIndicators) {
    SegmentOffsets segmentOffsets;
    SegmentedRecordInitializer recordInitializer;
    List<SegmentDefinition> segmentDefinitions = recordDefinition.getSegmentDefinitions();
    if (recordDefinition.equals(this.lastRecordDefinition) && segmentIndicators.equals(this.lastSegmentIndicators)) {
      segmentOffsets = this.lastSegmentOffsets;
      recordInitializer = this.lastSegmentedRecordInitializer;
    } else {
      if (segmentDefinitions.size() != segmentIndicators.size()) {
        throw new IllegalArgumentException("segment indicator list mismatch");
      }
      segmentOffsets = createSegmentOffsets(recordDefinition, segmentIndicators);

      Initializer baseInitializer = buildRecordInitializer(recordDefinition);
      List<SegmentInitializer> segmentInitializers = new ArrayList<>(segmentDefinitions.size());
      for (SegmentDefinition segmentDefinition : segmentDefinitions) {
        segmentInitializers.add(buildSegmentInitializer(segmentDefinition.getFields()));
      }
      recordInitializer = new SegmentedRecordInitializer(baseInitializer, segmentInitializers);
      
      this.lastRecordDefinition = recordDefinition;
      this.lastSegmentIndicators = segmentIndicators;
      this.lastSegmentOffsets = segmentOffsets;
      this.lastSegmentedRecordInitializer = recordInitializer;
    }
    // for caching maximum length would be better but segment allocator can not yet handle it
    int recordLength = recordDefinition.computeRecordLength(segmentOffsets);
    W line = instantiate(segmentOffsets, recordLength);
    line.doSetLength(recordLength);

    // first field is record type
    StringFieldDefinition recordDefinitionField = (StringFieldDefinition) recordDefinition.getFields().getFirst();
    line.writeString(recordDefinitionField, recordDefinition.getType());
    recordInitializer.initialize(line, recordDefinitionField.getLength(), segmentIndicators);

    // write the segment indicators afterwards, the we initialized with the default values
    for (int i = 0; i < segmentDefinitions.size(); i++) {
      StringFieldDefinition segmentIndicatorField = segmentDefinitions.get(i).getSegmentIndicatorField();
      var segmentIndicator = segmentIndicators.get(i);
      line.writeSegmentIndicator(segmentIndicatorField, segmentIndicator);
    }
    return line;
  }

  public W writingLineFor(FixedLengthRecordDefinition recordDefinition) {
    W line = instantiate(NoSegment.INSTANCE, recordDefinition.getMaximumLength());
    
    // first field is record type
    StringFieldDefinition recordDefinitionField = (StringFieldDefinition) recordDefinition.getFields().getFirst();
    line.doSetLength(recordDefinition.getMaximumLength());
    line.writeString(recordDefinitionField, recordDefinition.getType());

    Initializer initializer = buildRecordInitializer(recordDefinition);
    initializer.initialize(line, recordDefinitionField.getLength());
    return line;
  }

  protected abstract W instantiate(SegmentOffsets segmentOffsets, int recordLength);

  private SegmentOffsets createSegmentOffsets(SegmentedRecordDefinition recordDefinition, List<SegmentIndicator> segmentIndicators) {
    List<SegmentDefinition> segmentDefinitions = recordDefinition.getSegmentDefinitions();
    int offset = recordDefinition.getBaseLength();
    var segmentOffsets = new ArrayBasedSegmentOffsets(segmentDefinitions.size());
    for (int i = 0; i < segmentDefinitions.size(); i++) {
      var segmentDefinition = segmentDefinitions.get(i);
      int segmentIndicatorIndex = segmentDefinition.getSegmentIndicatorIndex();
      var segmentIndicator = segmentIndicators.get(segmentIndicatorIndex);
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

  static Initializer buildRecordInitializer(RecordDefinition recordDefinition) {
    List<? extends OffsetFieldDefinition> allFields = recordDefinition.getFields();
    // the first field is the record type, this has to be set always
    return buildFieldInitializer(allFields.subList(1, allFields.size()));
  }

  private static Initializer buildFieldInitializer(List<? extends OffsetFieldDefinition> fields) {
    List<Short> initializers = new ArrayList<>();
    OffsetFieldDefinition firstField = fields.getFirst();
    int currentLength = firstField.getLength();
    FieldType firstType = getType(firstField);
    FieldType previousType = firstType;
    for (OffsetFieldDefinition fieldDefinition : fields.subList(1, fields.size())) {
      FieldType currentType = getType(fieldDefinition);
      if (currentType == previousType) {
        // merge consecutive fields of the same type
        currentLength += fieldDefinition.getLength();
      } else {
        initializers.add(Utils.toPositiveShortExact(currentLength));

        currentLength = fieldDefinition.getLength();
        previousType = currentType;
      }
    }
    initializers.add(Utils.toPositiveShortExact(currentLength));
    return instantiateInitializer(firstType, initializers);
  }
  
  private static Initializer instantiateInitializer(FieldType firstType, List<Short> lengths) {
    short[] values = new short[lengths.size()];
    for (int i = 0; i < values.length; i++) {
      values[i] = lengths.get(i);
    }
    return switch(firstType) {
      case CHAR -> new CharTangoInitializer(values);
      case NUM -> new NumTangoInitializer(values);
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
        return 0;
      }
      return this.delegate.initialize(line, offset);
    }

  }

  sealed interface Initializer {

    int initialize(AbstractWritingLine line, int offset);

    /**
     * Exploits the fact that we have only interleaving NUM and CHAR types.
     */
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

      static final class CharTangoInitializer extends AbstractTangoInitializer {

        CharTangoInitializer(short[] fieldLengths) {
          super(fieldLengths);
        }

        @Override
        FieldType getInitialType() {
          return FieldType.CHAR;
        }

      }

      static final class NumTangoInitializer extends AbstractTangoInitializer {

        NumTangoInitializer(short[] fieldLengths) {
          super(fieldLengths);
        }

        @Override
        FieldType getInitialType() {
          return FieldType.NUM;
        }

      }

    }

  }

}
