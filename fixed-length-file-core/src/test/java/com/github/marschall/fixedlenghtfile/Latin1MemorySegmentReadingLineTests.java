package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;

class Latin1MemorySegmentReadingLineTests {

  private static final String LINE2 = "RFi\u00E9ld2Fi\u00E9l    \u00E9ld4  \u00E9ld       34";
  private static final String LINE1 = "RFi\u00E9ld1Fi\u00E9ld  i\u00E9ld3  \u00E9l        12";

  static final class R {

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

  private FileDefinition fileDefinition;

  @BeforeEach
  void setUp() {
    this.fileDefinition = new FileDefinition(List.of(R.definition()));
  }

  @Test
  void recordDefinitionLength() {
    var recordDefinition = this.fileDefinition.getRecordDefinition("R");
    assertNotNull(recordDefinition);
    assertEquals(33, recordDefinition.getMaximumLength());
  }

  @Test
  void readLines() throws IOException {

    var path = Path.of("src/test/resources/sample.txt");

    FixedLengthFileParser.parseFile(this.fileDefinition, path, file -> {
      file.parseLines((recordType, recordNumber, line) -> {
        assertEquals("R", recordType, "record type");
        if (recordNumber == 0) {
          assertEquals("Fi\u00E9ld1", line.readTrimmedString(R.FIELD1));
          assertEquals("Fi\u00E9ld", line.readTrimmedString(R.FIELD2));
          assertEquals("i\u00E9ld3", line.readTrimmedString(R.FIELD3));
          assertEquals("\u00E9l", line.readTrimmedString(R.FIELD4));
          assertSame("", line.readTrimmedString(R.FIELD5));
          assertEquals(12, line.readUnsignedInt(R.FIELD6));

        } else if (recordNumber == 1) {
          assertEquals("Fi\u00E9ld2", line.readTrimmedString(R.FIELD1));
          assertEquals("Fi\u00E9l", line.readTrimmedString(R.FIELD2));
          assertEquals("\u00E9ld4", line.readTrimmedString(R.FIELD3));
          assertEquals("\u00E9ld", line.readTrimmedString(R.FIELD4));
          assertSame("", line.readTrimmedString(R.FIELD5));
          assertEquals(34, line.readUnsignedInt(R.FIELD6));

        } else {
          fail(() -> "unexpected record number: " + recordNumber);
        }
      });
    });

  }

  @Test
  void asReader_readAllAsString() throws IOException {
    var path = Path.of("src/test/resources/sample.txt");

    FixedLengthFileParser.parseFile(this.fileDefinition, path, file -> {
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
    
    FixedLengthFileParser.parseFile(this.fileDefinition, path, file -> {
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

  @Disabled
  @Test
  void objectLayout() throws IOException {
    var path = Path.of("src/test/resources/sample.txt");

    FixedLengthFileParser.parseFile(this.fileDefinition, path, file -> {
      file.parseLines((_, _, line) -> {
        ClassLayout layout = ClassLayout.parseInstance(line);
        System.out.println(layout.toPrintable());
      });
    });
  }

}
