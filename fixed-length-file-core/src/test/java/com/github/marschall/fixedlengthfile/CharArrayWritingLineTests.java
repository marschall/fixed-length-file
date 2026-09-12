package com.github.marschall.fixedlengthfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;

class CharArrayWritingLineTests {

  static final class R {

    static final StringFieldDefinition TYPE;
    static final StringFieldDefinition FIELD1;
    static final StringFieldDefinition FIELD2;
    static final StringFieldDefinition FIELD3;
    static final UnsignedFieldDefinition FIELD6;
    static final UnsignedFieldDefinition FIELD7;

    static {
      TYPE = new StringFieldDefinition("TYPE", 1, 0);
      FIELD1 = new StringFieldDefinition("FIELD-1", 6, TYPE.getOffset() + TYPE.getLength());
      FIELD2 = new StringFieldDefinition("FIELD-2", 6, FIELD1.getOffset() + FIELD1.getLength());
      FIELD3 = new StringFieldDefinition("FIELD-3", 6, FIELD2.getOffset() + FIELD1.getLength());
      FIELD6 = new UnsignedFieldDefinition("FIELD-6", 2, FIELD3.getOffset() + FIELD3.getLength());
      FIELD7 = new UnsignedFieldDefinition("FIELD-7", 2, FIELD6.getOffset() + FIELD6.getLength());
    }

    static RecordDefinition definition() {
      return new FixedLengthRecordDefinition("R", List.of(TYPE, FIELD1, FIELD2, FIELD3, FIELD6, FIELD7));
    }

  }

  static final class H {

    static final StringFieldDefinition TYPE;
    static final UnsignedFieldDefinition DATE_FIELD1;
    static final UnsignedFieldDefinition TIME_FIELD1;
    static final UnsignedFieldDefinition DATE_FIELD2;
    static final UnsignedFieldDefinition TIME_FIELD2;
    static final UnsignedFieldDefinition DATE_FIELD3;
    static final UnsignedFieldDefinition TIME_FIELD3;
    static final UnsignedFieldDefinition BIG_INTEGER_FIELD;
    static final UnsignedFieldDefinition BIG_INTEGER_FIELD_EXPONENT;

    static {
      TYPE = new StringFieldDefinition("TYPE", 1, 0);
      DATE_FIELD1 = new UnsignedFieldDefinition("DATE-FIELD1", 8, TYPE.getOffset() + TYPE.getLength());
      TIME_FIELD1 = new UnsignedFieldDefinition("TIME-FIELD1", 6, DATE_FIELD1.getOffset() + DATE_FIELD1.getLength());
      DATE_FIELD2 = new UnsignedFieldDefinition("DATE-FIELD2", 8, TIME_FIELD1.getOffset() + TIME_FIELD1.getLength());
      TIME_FIELD2 = new UnsignedFieldDefinition("TIME-FIELD2", 6, DATE_FIELD2.getOffset() + DATE_FIELD2.getLength());
      DATE_FIELD3 = new UnsignedFieldDefinition("DATE-FIELD2", 8, TIME_FIELD2.getOffset() + TIME_FIELD2.getLength());
      TIME_FIELD3 = new UnsignedFieldDefinition("TIME-FIELD2", 8, DATE_FIELD3.getOffset() + DATE_FIELD3.getLength());
      BIG_INTEGER_FIELD = new UnsignedFieldDefinition("FIELD-6", 6, TIME_FIELD3.getOffset() + TIME_FIELD3.getLength());
      BIG_INTEGER_FIELD_EXPONENT = new UnsignedFieldDefinition("FIELD-7", 1, BIG_INTEGER_FIELD.getOffset() + BIG_INTEGER_FIELD.getLength());
    }

    static RecordDefinition definition() {
      return new FixedLengthRecordDefinition("H", List.of(TYPE, DATE_FIELD1, TIME_FIELD1, DATE_FIELD2, TIME_FIELD2, DATE_FIELD3, TIME_FIELD3, BIG_INTEGER_FIELD, BIG_INTEGER_FIELD_EXPONENT));
    }

  }

  private static String writeLine(RecordDefinition recordDefinition, Consumer<WritingLine> lineConsumer) throws IOException {
    FixedLengthRecordDefinition definition = (FixedLengthRecordDefinition) recordDefinition;
    int maximumLength = definition.getMaximumLength();
    var line = new CharArrayWritingLine(maximumLength);
    line.initializeFor(definition);

    lineConsumer.accept(line);

    try (StringWriter writer = new StringWriter(maximumLength)) {
      line.exportTo(writer);
      return writer.toString();
    }
  }

  @Test
  void writeContent() throws IOException {
    String content = writeLine(R.definition(), line -> {
      line.writeString(R.FIELD1, "Fi\u00E9ld1");
      line.writeString(R.FIELD2, "Fi\u00E9ld");
      line.writeString(R.FIELD3, null);
      line.writeUnsignedInt(R.FIELD6, 12);
      line.writeUnsignedInt(R.FIELD7, 3);
    });
    assertEquals("RFi\u00E9ld1Fi\u00E9ld       1203", content);
  }

  @Test
  void writeEmpty() throws IOException {
    String content = writeLine(R.definition(), _ -> {
      // empty
    });
    assertEquals("R                  0000", content);
  }

  @Test
  void writeInvalid() throws IOException {
    writeLine(R.definition(), line -> {
      assertThrows(IllegalArgumentException.class, () -> line.writeString(R.FIELD1, "Field12"));
      assertThrows(IllegalArgumentException.class, () -> line.writeUnsignedInt(R.FIELD6, -1));
      assertThrows(IllegalArgumentException.class, () -> line.writeUnsignedInt(R.FIELD6, 100));
    });
  }

  @Test
  void writeHighLevel() throws IOException {
    String content = writeLine(H.definition(), line -> {
      line.writeLocalDate(H.DATE_FIELD1, LocalDate.of(2001, 4, 5));
      line.writeLocalTime(H.TIME_FIELD1, LocalTime.of(10, 15, 23));
      line.writeLocalDateTime(H.DATE_FIELD2, H.TIME_FIELD2, LocalDateTime.of(LocalDate.of(2008, 1, 15), LocalTime.of(16, 53, 8)));
      line.writeLocalDateTime(H.DATE_FIELD3, H.TIME_FIELD3, LocalDateTime.of(LocalDate.of(2015, 4, 14), LocalTime.of(12, 28, 42, 123456789)));
      line.writeBigDecimal(H.BIG_INTEGER_FIELD, H.BIG_INTEGER_FIELD_EXPONENT, BigDecimal.valueOf(12345L, 2), 2);
    });
    assertEquals("H200104051015152008011516535320150414122829230123452", content);
  }

}
