package com.github.marschall.fixedlenghtfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;

class HighLevelLatin1MemorySegmentReadingLineTests {

  static final class R {

    static final StringFieldDefinition TYPE;
    static final UnsignedFieldDefinition DATE_FIELD;
    static final UnsignedFieldDefinition TIME_FIELD6;
    static final UnsignedFieldDefinition TIME_FIELD8;
    static final UnsignedFieldDefinition AMOUNT_FIELD;
    static final UnsignedFieldDefinition EXPONENT_FIELD;
    
    static {
      TYPE = new StringFieldDefinition("TYPE", 1, 0);
      DATE_FIELD = new UnsignedFieldDefinition("DATE-FIELD", 8, TYPE.getOffset() + TYPE.getLength());
      TIME_FIELD6 = new UnsignedFieldDefinition("TIME-FIELD-6", 6, DATE_FIELD.getOffset() + DATE_FIELD.getLength());
      TIME_FIELD8 = new UnsignedFieldDefinition("TIME-FIELD-8", 8, TIME_FIELD6.getOffset() + TIME_FIELD6.getLength());
      AMOUNT_FIELD = new UnsignedFieldDefinition("AMOUNT-FIELD", 12, TIME_FIELD8.getOffset() + TIME_FIELD8.getLength());
      EXPONENT_FIELD = new UnsignedFieldDefinition("EXPONENT-FIELD", 1, AMOUNT_FIELD.getOffset() + AMOUNT_FIELD.getLength());
    }

    static RecordDefinition definition() {
      return new FixedLengthRecordDefinition("R", List.of(TYPE, DATE_FIELD, TIME_FIELD6, TIME_FIELD8, AMOUNT_FIELD, EXPONENT_FIELD));
    }

  }

  private FixedLengthFileParser parser;

  @BeforeEach
  void setUp() {
    this.parser = new FixedLengthFileParser();
  }


  @Test
  void readHighLevelTypes() throws IOException {

    var path = Path.of("src/test/resources/sample_high_level_types");

    FileDefinition highLevelDefinition = new FileDefinition(List.of(R.definition()));

    this.parser.parseFile(highLevelDefinition, path, file -> {
      file.parseFile((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        assertEquals(0, recordNumber, "record number");

        assertEquals(LocalDate.of(2026, 8, 9), line.readLocalDate(R.DATE_FIELD));
        assertEquals(LocalTime.of(20, 52, 13), line.readLocalTime(R.TIME_FIELD6));
        assertEquals(LocalTime.of(20, 52, 14, 560_000_000), line.readLocalTime(R.TIME_FIELD8));

        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 13)), line.readLocalDateTime(R.DATE_FIELD, R.TIME_FIELD6));
        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 14, 560_000_000)), line.readLocalDateTime(R.DATE_FIELD, R.TIME_FIELD8));

        assertThat(line.readBigDecimal(R.AMOUNT_FIELD, R.EXPONENT_FIELD)).isEqualByComparingTo(new BigDecimal("1234567890.12"));
      });
    });
  }

}
