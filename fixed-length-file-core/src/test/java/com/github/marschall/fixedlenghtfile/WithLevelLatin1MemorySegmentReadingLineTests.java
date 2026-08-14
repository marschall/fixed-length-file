package com.github.marschall.fixedlenghtfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.BoundField.BoundBigDecimalField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLocalDateTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalDateField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalTimeField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

class WithLevelLatin1MemorySegmentReadingLineTests {

  static final class R {

    static final StringFieldDefinition TYPE = new StringFieldDefinition("TYPE", 1);

    static final UnsignedFieldDefinition DATE_FIELD = new UnsignedFieldDefinition("DATE-FIELD", 8);

    static final UnsignedFieldDefinition TIME_FIELD6 = new UnsignedFieldDefinition("TIME-FIELD-6", 6);

    static final UnsignedFieldDefinition TIME_FIELD8 = new UnsignedFieldDefinition("TIME-FIELD-8", 8);

    static final UnsignedFieldDefinition AMOUNT_FIELD = new UnsignedFieldDefinition("AMOUNT-FIELD", 12);

    static final UnsignedFieldDefinition EXPONENT_FIELD = new UnsignedFieldDefinition("EXPONENT-FIELD", 1);

  }

  private FixedLengthFileParser parser;

  @BeforeEach
  void setUp() {
    this.parser = new FixedLengthFileParser();
  }


  @Test
  void readHighLevelTypes() throws IOException {

    var path = Path.of("src/test/resources/sample_high_level_types");

    FileDefinition highLevelDefinition = FileDefinition.builder()
        .defineRecordType("R", binder -> {
          binder.bind(R.TYPE);
          binder.bind(R.DATE_FIELD);
          binder.bind(R.TIME_FIELD6);
          binder.bind(R.TIME_FIELD8);
          binder.bind(R.AMOUNT_FIELD);
          binder.bind(R.EXPONENT_FIELD);
        })
        .build();

    var recordDefinition = highLevelDefinition.getRecordDefinition("R");
    BoundLocalDateField dateField = recordDefinition.bindLocalDateField(R.DATE_FIELD);
    BoundLocalTimeField timeField6 = recordDefinition.bindLocalTimeField(R.TIME_FIELD6);
    BoundLocalTimeField timeField8 = recordDefinition.bindLocalTimeField(R.TIME_FIELD8);
    BoundLocalDateTimeField localDateTimeField6 = recordDefinition.bindLocalDateTimeField(R.DATE_FIELD, R.TIME_FIELD6);
    BoundLocalDateTimeField localDateTimeField8 = recordDefinition.bindLocalDateTimeField(R.DATE_FIELD, R.TIME_FIELD8);
    BoundBigDecimalField bigDecimalField = recordDefinition.bindBigDecimalField(R.AMOUNT_FIELD, R.EXPONENT_FIELD);

    this.parser.parseFile(highLevelDefinition, path, file -> {
      file.parseFile((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        assertEquals(0, recordNumber, "record number");

        assertEquals(LocalDate.of(2026, 8, 9), line.readLocalDate(dateField));
        assertEquals(LocalTime.of(20, 52, 13), line.readLocalTime(timeField6));
        assertEquals(LocalTime.of(20, 52, 14, 560_000_000), line.readLocalTime(timeField8));

        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 13)), line.readLocalDateTime(localDateTimeField6));
        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 14, 560_000_000)), line.readLocalDateTime(localDateTimeField8));

        assertThat(line.readBigDecimal(bigDecimalField)).isEqualByComparingTo(new BigDecimal("1234567890.12"));
      });
    });
  }

}
