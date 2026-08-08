package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import com.github.marschall.fixedlenghtfile.BoundField.BoundBigDecimalField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLocalDateTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalDateField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;

class Latin1MemorySegmentReadingLineTests {

  private FixedLengthFileParser parser;
  private FileDefinition fileDefinition;

  @BeforeEach
  void setUp() {
    this.fileDefinition = FileDefinition.builder()
        .defineRecordType("R", binder -> {
          binder.bind(FieldDefinitions.TYPE);
          binder.bind(FieldDefinitions.FIELD1);
          binder.bind(FieldDefinitions.FIELD2);
          binder.bind(FieldDefinitions.FIELD3);
          binder.bind(FieldDefinitions.FIELD4);
          binder.bind(FieldDefinitions.FIELD5);
          binder.bind(FieldDefinitions.FIELD6);
        })
      .build();
    
    this.parser = new FixedLengthFileParser();
  }
  
  @Test
  void recordDefinitionLength() {
    var recordDefinition = this.fileDefinition.getRecordDefinition("R");
    assertNotNull(recordDefinition);
    assertEquals(33, recordDefinition.getMaxiumLength());
  }

  @Test
  void readLines() throws IOException {

    var path = Path.of("src/test/resources/sample.txt");

    var recordDefinition = this.fileDefinition.getRecordDefinition("R");
    BoundStringField field1 = recordDefinition.bindStringField(FieldDefinitions.FIELD1);
    BoundStringField field2 = recordDefinition.bindStringField(FieldDefinitions.FIELD2);
    BoundStringField field3 = recordDefinition.bindStringField(FieldDefinitions.FIELD3);
    BoundStringField field4 = recordDefinition.bindStringField(FieldDefinitions.FIELD4);
    BoundStringField field5 = recordDefinition.bindStringField(FieldDefinitions.FIELD5);
    BoundIntegerField field6 = recordDefinition.bindUnsingedIntegerField(FieldDefinitions.FIELD6);

    this.parser.parseFile(this.fileDefinition, path, file -> {
      file.parseFile((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        if (recordNumber == 0) {
          assertEquals("Fi\u00E9ld1", line.readTrimmedString(field1));
          assertEquals("Fi\u00E9ld", line.readTrimmedString(field2));
          assertEquals("i\u00E9ld3", line.readTrimmedString(field3));
          assertEquals("\u00E9l", line.readTrimmedString(field4));
          assertSame("", line.readTrimmedString(field5));
          assertEquals(12, line.readUnsignedInt(field6));

          String content;
          try {
            content = line.asReader().readAllAsString();
          } catch (IOException e) {
            fail(e);
            return;
          }
          assertEquals("RFi\u00E9ld1Fi\u00E9ld  i\u00E9ld3  \u00E9l        12", content);
          
        } else if (recordNumber == 1) {
          assertEquals("Fi\u00E9ld2", line.readTrimmedString(field1));
          assertEquals("Fi\u00E9l", line.readTrimmedString(field2));
          assertEquals("\u00E9ld4", line.readTrimmedString(field3));
          assertEquals("\u00E9ld", line.readTrimmedString(field4));
          assertSame("", line.readTrimmedString(field5));
          assertEquals(34, line.readUnsignedInt(field6));

          String content;
          try {
            content = line.asReader().readAllAsString();
          } catch (IOException e) {
            fail(e);
            return;
          }
          assertEquals("RFi\u00E9ld2Fi\u00E9l    \u00E9ld4  \u00E9ld       34", content);
        } else {
          fail(() -> "unexpected record number: " + recordNumber);
        }
      });
    });

  }

  @Test
  void readHighLevelTypes() throws IOException {

    var path = Path.of("src/test/resources/sample_high_level_types");

    FileDefinition highLevelDefinition = FileDefinition.builder()
        .defineRecordType("R", binder -> {
          binder.bind(FieldDefinitions.TYPE);
          binder.bind(FieldDefinitions.DATE_FIELD);
          binder.bind(FieldDefinitions.TIME_FIELD6);
          binder.bind(FieldDefinitions.TIME_FIELD8);
          binder.bind(FieldDefinitions.AMOUNT_FIELD);
          binder.bind(FieldDefinitions.EXPONENT_FIELD);
        })
        .build();

    var recordDefinition = this.fileDefinition.getRecordDefinition("R");
    BoundLocalDateField dateField = recordDefinition.bindLocalDateField(FieldDefinitions.DATE_FIELD);
    BoundLocalTimeField timeField6 = recordDefinition.bindLocalTimeField(FieldDefinitions.TIME_FIELD6);
    BoundLocalTimeField timeField8 = recordDefinition.bindLocalTimeField(FieldDefinitions.TIME_FIELD8);
    BoundLocalDateTimeField localDateTimeField6 = recordDefinition.bindLocalDateTimeField(FieldDefinitions.DATE_FIELD, FieldDefinitions.TIME_FIELD6);
    BoundLocalDateTimeField localDateTimeField8 = recordDefinition.bindLocalDateTimeField(FieldDefinitions.DATE_FIELD, FieldDefinitions.TIME_FIELD8);
    BoundBigDecimalField bigDecimalField = recordDefinition.bindBigDecimalField(FieldDefinitions.AMOUNT_FIELD, FieldDefinitions.EXPONENT_FIELD);

    this.parser.parseFile(highLevelDefinition, path, file -> {
      file.parseFile((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        assertEquals(0, recordNumber, "record number");

        assertEquals(LocalDate.of(2026, 8, 9), line.readLocalDate(dateField));
        assertEquals(LocalTime.of(20, 52, 13), line.readLocalTime(timeField6));
        assertEquals(LocalTime.of(20, 52, 14, 560), line.readLocalTime(timeField8));

        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 13)), line.readLocalDateTime(localDateTimeField6));
        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 14, 560)), line.readLocalDateTime(localDateTimeField8));

        assertThat(line.readBigDecimal(bigDecimalField)).isEqualByComparingTo(new BigDecimal("1234567890.12"));
      });
    });
  }

  @Disabled
  @Test
  void objectLayout() throws IOException {
    var path = Path.of("src/test/resources/sample.txt");

    this.parser.parseFile(this.fileDefinition, path, file -> {
      file.parseFile((_, _, line) -> {
        ClassLayout layout = ClassLayout.parseInstance(line);
        System.out.println(layout.toPrintable());
      });
    });
  }

}
