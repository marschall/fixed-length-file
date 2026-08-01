package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;

class FixedLengthFileParserTests {
  
  private FixedLengthFileParser parser;

  @BeforeEach
  void setUp() {
    this.parser = new FixedLengthFileParser();
  }

  @Test
  void sampleWithRecordTypes() throws IOException {
    Path path = Path.of("src/test/resources/sample_with_record_types.txt");
    RecordDefinition headerDefinition = new FixedLengthRecordDefinition("H", 2);
    RecordDefinition recordDefintion = new SegmentedRecordDefinition("R", 10,
        List.of(
            new SegmentDefinition(7, 1),
            new SegmentDefinition(8, 1),
            new SegmentDefinition(9, 1)));
    RecordDefinition footerDefinition = new FixedLengthRecordDefinition("F", 2);
    FileDefinition fileDefinition = new FileDefinition(List.of(headerDefinition, recordDefintion, footerDefinition));
    AtomicInteger expectedRecordNumber = new AtomicInteger(0);
    List<String> expectedRecordTypes = List.of("H", "R", "R", "R", "F");
    this.parser.parseFile(fileDefinition, path, file -> {
      file.parseFile((recordType, recordNumber, line) -> {
        assertEquals(expectedRecordNumber.getAndIncrement(), recordNumber, "record number");
        assertEquals(expectedRecordTypes.get(recordNumber), recordType, "record type");
        assertNotNull(line, "line");
      });
    });
    assertEquals(5, expectedRecordNumber.get(), "encounterd records");
  }

}
