package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;

class FixedLengthFileParserTests {
  

  static final StringFieldDefinition TYPE = new StringFieldDefinition("TYPE", 1);
  
  static final StringFieldDefinition FIELD1 = new StringFieldDefinition("FIELD-1", 1);
  static final StringFieldDefinition FIELD2 = new StringFieldDefinition("FIELD-2", 3);
  static final UnsignedFieldDefinition FIELD3 = new UnsignedFieldDefinition("FIELD-2", 3);
  
  static final StringFieldDefinition S1 = new StringFieldDefinition("S1", 1);
  static final StringFieldDefinition S2 = new StringFieldDefinition("S2", 1);
  static final StringFieldDefinition S3 = new StringFieldDefinition("S3", 1);
  
  static final StringFieldDefinition S1_1 = new StringFieldDefinition("S1-1", 1);
  static final StringFieldDefinition S2_1 = new StringFieldDefinition("S2-1", 1);
  static final StringFieldDefinition S3_1 = new StringFieldDefinition("S3-1", 1);

  private FixedLengthFileParser parser;

  @BeforeEach
  void setUp() {
    this.parser = new FixedLengthFileParser();
  }

  @Test
  void sampleWithRecordTypes() throws IOException {
    Path path = Path.of("src/test/resources/sample_with_record_types.txt");

    FileDefinition fileDefinition = FileDefinition.builder()
        .defineRecordType("H", binder -> {
          binder.bind(TYPE);
          binder.bind(FIELD1);
        })
        .defineRecordType("R", binder -> {
          binder.bind(TYPE);
          binder.bind(FIELD2);
          binder.bind(FIELD3);
          binder.defineSegment(S1, segmentBinder -> {
            segmentBinder.bind(S1_1);
          });
          binder.defineSegment(S2, segmentBinder -> {
            segmentBinder.bind(S2_1);
          });
          binder.defineSegment(S3, segmentBinder -> {
            segmentBinder.bind(S3_1);
          });
        })
        .defineRecordType("F", binder -> {
          binder.bind(TYPE);
          binder.bind(FIELD1);
        })
        .build();

    RecordDefinition headerDefinition = fileDefinition.getRecordDefinition("H");
    BoundStringField headerType = headerDefinition.bindStringField(TYPE);
    BoundStringField headerStringField = headerDefinition.bindStringField(FIELD1);
    assertEquals(2, headerDefinition.getMaximumLength());
    
    RecordDefinition recordDefinition = fileDefinition.getRecordDefinition("R");
    BoundStringField recordTypeField = recordDefinition.bindStringField(TYPE);
    BoundSegmentIndicatorField indicator1 = recordDefinition.bindSegmentIndicatorField(S1);
    BoundSegmentIndicatorField indicator2 = recordDefinition.bindSegmentIndicatorField(S2);
    BoundSegmentIndicatorField indicator3 = recordDefinition.bindSegmentIndicatorField(S3);
    assertEquals(13, recordDefinition.getMaximumLength());
    
    RecordDefinition footerDefinition = fileDefinition.getRecordDefinition("F");
    BoundStringField footerType = footerDefinition.bindStringField(TYPE);
    BoundStringField footerStringField = footerDefinition.bindStringField(FIELD1);
    assertEquals(2, footerDefinition.getMaximumLength());

    AtomicInteger expectedRecordNumber = new AtomicInteger(0);
    List<String> expectedRecordTypes = List.of("H", "R", "R", "R", "F");
    List<Integer> expectedLenghts = List.of(2, 13, 10, 13, 2);
    this.parser.parseFile(fileDefinition, path, file -> {
      file.parseFile((recordType, recordNumber, line) -> {
        assertEquals(expectedRecordNumber.getAndIncrement(), recordNumber, "record number");
        assertEquals(expectedRecordTypes.get(recordNumber), recordType, "record type");
        assertEquals(expectedLenghts.get(recordNumber), line.getLength(), "line length");
        assertNotNull(line, "line");

        switch (recordType) {
          case "H" -> {
            assertEquals("H", line.readTrimmedString(headerType), "header type");
            assertEquals("1", line.readTrimmedString(headerStringField), "header field 1");
          }
          case "F" -> {
            assertEquals("F", line.readTrimmedString(footerType), "footer type");
            assertEquals("2", line.readTrimmedString(footerStringField), "footer field 1");
          }
          case "R" -> {
            assertEquals("R", line.readTrimmedString(recordTypeField), "record type");
            switch (recordNumber) {
              case 1 -> {
                assertSame(SegmentIndicator.PRESENT, line.readSegmentIndicator(indicator1));
                assertSame(SegmentIndicator.PRESENT, line.readSegmentIndicator(indicator2));
                assertSame(SegmentIndicator.PRESENT, line.readSegmentIndicator(indicator3));
              }
              case 2 -> {
                assertSame(SegmentIndicator.ABSENT, line.readSegmentIndicator(indicator1));
                assertSame(SegmentIndicator.ABSENT, line.readSegmentIndicator(indicator2));
                assertSame(SegmentIndicator.ABSENT, line.readSegmentIndicator(indicator3));
              }
              case 3 -> {
                assertSame(SegmentIndicator.SPACES, line.readSegmentIndicator(indicator1));
                assertSame(SegmentIndicator.SPACES, line.readSegmentIndicator(indicator2));
                assertSame(SegmentIndicator.SPACES, line.readSegmentIndicator(indicator3));
              }
            }
          }
        }
      });
    });
    assertEquals(5, expectedRecordNumber.get(), "encounterd records");
  }

}
