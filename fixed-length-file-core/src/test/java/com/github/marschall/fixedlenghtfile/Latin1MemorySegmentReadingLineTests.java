package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

class Latin1MemorySegmentReadingLineTests {
  
  static final class R {


    static final StringFieldDefinition TYPE = new StringFieldDefinition("TYPE", 1);

    static final StringFieldDefinition FIELD1 = new StringFieldDefinition("FIELD-1", 6);
    
    static final StringFieldDefinition FIELD2 = new StringFieldDefinition("FIELD-2", 6);

    static final StringFieldDefinition FIELD3 = new StringFieldDefinition("FIELD-3", 6);
    
    static final StringFieldDefinition FIELD4 = new StringFieldDefinition("FIELD-4", 6);
    
    static final StringFieldDefinition FIELD5 = new StringFieldDefinition("FIELD-5", 6);

    static final UnsignedFieldDefinition FIELD6 = new UnsignedFieldDefinition("FIELD-6", 2);
    
    static final UnsignedFieldDefinition FIELD7 = new UnsignedFieldDefinition("FIELD-7", 2);
    
  }

  private FixedLengthFileParser parser;
  private FileDefinition fileDefinition;

  @BeforeEach
  void setUp() {
    this.fileDefinition = FileDefinition.builder()
        .defineRecordType("R", binder -> {
          binder.bind(R.TYPE);
          binder.bind(R.FIELD1);
          binder.bind(R.FIELD2);
          binder.bind(R.FIELD3);
          binder.bind(R.FIELD4);
          binder.bind(R.FIELD5);
          binder.bind(R.FIELD6);
        })
      .build();
    
    this.parser = new FixedLengthFileParser();
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

    var recordDefinition = this.fileDefinition.getRecordDefinition("R");
    BoundStringField field1 = recordDefinition.bindStringField(R.FIELD1);
    BoundStringField field2 = recordDefinition.bindStringField(R.FIELD2);
    BoundStringField field3 = recordDefinition.bindStringField(R.FIELD3);
    BoundStringField field4 = recordDefinition.bindStringField(R.FIELD4);
    BoundStringField field5 = recordDefinition.bindStringField(R.FIELD5);
    BoundIntegerField field6 = recordDefinition.bindUnsingedIntegerField(R.FIELD6);

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
