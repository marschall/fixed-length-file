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

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;

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
    var recordDefinition = this.fileDefinition.getRecordDefinitionMap().get("R");
    assertNotNull(recordDefinition);
    assertEquals(33, recordDefinition.getLength());
  }

  @Test
  void readLines() throws IOException {
    
    Path path = Path.of("src/test/resources/sample.txt");

    FieldBinder binder = new FieldBinder();
    binder.bind(FieldDefinitions.TYPE);
    BoundStringField field1 = binder.bind(FieldDefinitions.FIELD1);
    BoundStringField field2 = binder.bind(FieldDefinitions.FIELD2);
    BoundStringField field3 = binder.bind(FieldDefinitions.FIELD3);
    BoundStringField field4 = binder.bind(FieldDefinitions.FIELD4);
    BoundStringField field5 = binder.bind(FieldDefinitions.FIELD5);
    BoundIntegerField field6 = binder.bind(FieldDefinitions.FIELD6);

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
    Path path = Path.of("src/test/resources/sample.txt");

    this.parser.parseFile(this.fileDefinition, path, file -> {
      file.parseFile((_, _, line) -> {
        ClassLayout layout = ClassLayout.parseInstance(line);
        System.out.println(layout.toPrintable());
      });
    });
  }

}
