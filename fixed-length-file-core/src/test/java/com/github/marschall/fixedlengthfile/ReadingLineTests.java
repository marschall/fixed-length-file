package com.github.marschall.fixedlengthfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FileDefinition.Version;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;

class ReadingLineTests {

  private static final FileDefinition FILE_DEFINITION2 = new FileDefinition(Version.of(1, 0), List.of(R2.definition()));
  private static final FileDefinition FILE_DEFINITION1 = new FileDefinition(Version.of(1, 0), List.of(R1.definition()));
  private static final String LINE2 = "RFi\u00E9ld2Fi\u00E9l    \u00E9ld4  \u00E9ld       34";
  private static final String LINE1 = "RFi\u00E9ld1Fi\u00E9ld  i\u00E9ld3  \u00E9l        12";

  static final class R1 {

    static final StringFieldDefinition TYPE;
    static final StringFieldDefinition FIELD1;
    static final StringFieldDefinition FIELD2;
    static final StringFieldDefinition FIELD3;
    static final StringFieldDefinition FIELD4;
    static final StringFieldDefinition FIELD5;
    static final UnsignedFieldDefinition FIELD6;
    static final UnsignedFieldDefinition FIELD7;

    static {
      TYPE = new StringFieldDefinition("TYPE", 1, 0);
      FIELD1 = new StringFieldDefinition("FIELD-1", 6, TYPE.getOffset() + TYPE.getLength());
      FIELD2 = new StringFieldDefinition("FIELD-2", 6, FIELD1.getOffset() + FIELD1.getLength());
      FIELD3 = new StringFieldDefinition("FIELD-3", 6, FIELD2.getOffset() + FIELD1.getLength());
      FIELD4 = new StringFieldDefinition("FIELD-4", 6, FIELD3.getOffset() + FIELD3.getLength());
      FIELD5 = new StringFieldDefinition("FIELD-5", 6, FIELD4.getOffset() + FIELD4.getLength());
      FIELD6 = new UnsignedFieldDefinition("FIELD-6", 2, FIELD5.getOffset() + FIELD5.getLength());
      FIELD7 = new UnsignedFieldDefinition("FIELD-7", 2, FIELD6.getOffset() + FIELD6.getLength());
    }

    static RecordDefinition definition() {
      return new FixedLengthRecordDefinition("R", List.of(TYPE, FIELD1, FIELD2, FIELD3, FIELD4, FIELD5, FIELD6));
    }

  }

  static final class R2 {

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

  @Test
  void recordDefinitionLength() {
    var fileDefinition = FILE_DEFINITION1;
    var recordDefinition = fileDefinition.getRecordDefinition("R");
    assertNotNull(recordDefinition);
    assertEquals(33, recordDefinition.getMaximumLength());
  }
  
  private FixedLengthFileParser parser1() {
    return new FixedLengthFileParser(FileDefinitionRepository.builder()
        .addMainFileDefinition(FILE_DEFINITION1)
        .build());
  }
  
  private FixedLengthFileParser parser2() {
    return new FixedLengthFileParser(FileDefinitionRepository.builder()
        .addMainFileDefinition(FILE_DEFINITION2)
        .build());
  }

  @Test
  void readLines() throws IOException {

    var path = Path.of("src/test/resources/sample.txt");

    this.parser1().parseFile(path, file -> {
      file.parseLines((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        if (recordNumber == 0) {
          assertEquals("Fi\u00E9ld1", line.readTrimmedString(R1.FIELD1));
          assertEquals("Fi\u00E9ld", line.readTrimmedString(R1.FIELD2));
          assertEquals("i\u00E9ld3", line.readTrimmedString(R1.FIELD3));
          assertEquals("\u00E9l", line.readTrimmedString(R1.FIELD4));
          assertSame("", line.readTrimmedString(R1.FIELD5));
          assertEquals(12, line.readUnsignedInt(R1.FIELD6));

        } else if (recordNumber == 1) {
          assertEquals("Fi\u00E9ld2", line.readTrimmedString(R1.FIELD1));
          assertEquals("Fi\u00E9l", line.readTrimmedString(R1.FIELD2));
          assertEquals("\u00E9ld4", line.readTrimmedString(R1.FIELD3));
          assertEquals("\u00E9ld", line.readTrimmedString(R1.FIELD4));
          assertSame("", line.readTrimmedString(R1.FIELD5));
          assertEquals(34, line.readUnsignedInt(R1.FIELD6));

        } else {
          fail(() -> "unexpected record number: " + recordNumber);
        }
      });
    });

  }

  @Test
  void asReader_readAllAsString() throws IOException {
    var path = Path.of("src/test/resources/sample.txt");

    this.parser1().parseFile(path, file -> {
      file.parseLines((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        if (recordNumber == 0) {
          String content;
          try {
            content = line.asReader().readAllAsString();
          } catch (IOException e) {
            fail(e);
            return;
          }
          assertEquals(LINE1, content);

        } else if (recordNumber == 1) {
          String content;
          try {
            content = line.asReader().readAllAsString();
          } catch (IOException e) {
            fail(e);
            return;
          }
          assertEquals(LINE2, content);
        } else {
          fail(() -> "unexpected record number: " + recordNumber);
        }
      });
    });
  }

  @Test
  void asReader_read() throws IOException {
    var path = Path.of("src/test/resources/sample.txt");
    
    this.parser1().parseFile(path, file -> {
      file.parseLines((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        if (recordNumber == 0) {
          int read;
          char[] content = new char[128];
          try {
            Reader reader = line.asReader();
            read = reader.read(content, 1, LINE1.length());
          } catch (IOException e) {
            fail(e);
            return;
          }
          assertEquals(read, LINE1.length());
          char[] expected = new char[content.length];
          LINE1.getChars(0, LINE1.length(), expected, 1);
          assertArrayEquals(expected, content);
          
        } else if (recordNumber == 1) {
          int read;
          char[] content = new char[128];
          try {
            Reader reader = line.asReader();
            read = reader.read(content, 1, LINE2.length());
          } catch (IOException e) {
            fail(e);
            return;
          }
          assertEquals(read, LINE2.length());
          char[] expected = new char[content.length];
          LINE2.getChars(0, LINE2.length(), expected, 1);
          assertArrayEquals(expected, content);
        } else {
          fail(() -> "unexpected record number: " + recordNumber);
        }
      });
    });
  }

  @Test
  void bufferedReadingLine1() throws IOException {
    var fileDefinition = FILE_DEFINITION1;
    var line = new BufferedReadingLine(fileDefinition);
    line.initializeFrom(new StringReader(LINE1));

    assertEquals("Fi\u00E9ld1", line.readTrimmedString(R1.FIELD1));
    assertEquals("Fi\u00E9ld", line.readTrimmedString(R1.FIELD2));
    assertEquals("i\u00E9ld3", line.readTrimmedString(R1.FIELD3));
    assertEquals("\u00E9l", line.readTrimmedString(R1.FIELD4));
    assertSame("", line.readTrimmedString(R1.FIELD5));
    assertEquals(12, line.readUnsignedInt(R1.FIELD6));
  }

  @Test
  void bufferedReadingLine2() throws IOException {
    var fileDefinition = FILE_DEFINITION1;
    var line = new BufferedReadingLine(fileDefinition);
    line.initializeFrom(new StringReader(LINE2));

    assertEquals("Fi\u00E9ld2", line.readTrimmedString(R1.FIELD1));
    assertEquals("Fi\u00E9l", line.readTrimmedString(R1.FIELD2));
    assertEquals("\u00E9ld4", line.readTrimmedString(R1.FIELD3));
    assertEquals("\u00E9ld", line.readTrimmedString(R1.FIELD4));
    assertSame("", line.readTrimmedString(R1.FIELD5));
    assertEquals(34, line.readUnsignedInt(R1.FIELD6));
  }


  @Test
  void readHighLevelTypes() throws IOException {

    var path = Path.of("src/test/resources/sample_high_level_types");


    this.parser2().parseFile(path, file -> {
      file.parseLines((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        assertEquals(0, recordNumber, "record number");

        assertEquals(LocalDate.of(2026, 8, 9), line.readLocalDate(R2.DATE_FIELD));
        assertEquals(LocalTime.of(20, 52, 13), line.readLocalTime(R2.TIME_FIELD6));
        assertEquals(LocalTime.of(20, 52, 14, 560_000_000), line.readLocalTime(R2.TIME_FIELD8));

        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 13)), line.readLocalDateTime(R2.DATE_FIELD, R2.TIME_FIELD6));
        assertEquals(LocalDateTime.of(LocalDate.of(2026, 8, 9), LocalTime.of(20, 52, 14, 560_000_000)), line.readLocalDateTime(R2.DATE_FIELD, R2.TIME_FIELD8));

        assertThat(line.readBigDecimal(R2.AMOUNT_FIELD, R2.EXPONENT_FIELD)).isEqualByComparingTo(new BigDecimal("1234567890.12"));
      });
    });
  }

  @Disabled
  @Test
  void objectLayout() throws IOException {
    var path = Path.of("src/test/resources/sample.txt");

    this.parser1().parseFile(path, file -> {
      file.parseLines((_, _, line) -> {
        ClassLayout layout = ClassLayout.parseInstance(line);
        System.out.println(layout.toPrintable());
      });
    });
  }

}
