package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

//    RecordDefinition headerDefinition = new FixedLengthRecordDefinition("H", 2);
//    RecordDefinition recordDefintion = new SegmentedRecordDefinition("R", 10,
//            List.of(
//                    new SegmentDefinition(7, 1),
//                    new SegmentDefinition(8, 1),
//                    new SegmentDefinition(9, 1)));
//    RecordDefinition footerDefinition = new FixedLengthRecordDefinition("F", 2);
//    FileDefinition fileDefinition = new FileDefinition(List.of(headerDefinition, recordDefintion, footerDefinition));
    Map<String,RecordDefinition> recordDefinitionMap = fileDefinition.getRecordDefinitionMap();
    assertEquals(2, recordDefinitionMap.get("H").getLength());
    assertEquals(2, recordDefinitionMap.get("F").getLength());

    AtomicInteger expectedRecordNumber = new AtomicInteger(0);
    List<String> expectedRecordTypes = List.of("H", "R", "R", "R", "F");
    List<Integer> expectedLenghts = List.of(2, 13, 10, 13, 2);
    this.parser.parseFile(fileDefinition, path, file -> {
      file.parseFile((recordType, recordNumber, line) -> {
        assertEquals(expectedRecordNumber.getAndIncrement(), recordNumber, "record number");
        assertEquals(expectedRecordTypes.get(recordNumber), recordType, "record type");
        assertEquals(expectedLenghts.get(recordNumber), line.getLength(), "line length");
        assertNotNull(line, "line");
      });
    });
    assertEquals(5, expectedRecordNumber.get(), "encounterd records");
  }

}
