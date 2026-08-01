package com.github.marschall.fixedlenghtfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentDefinition;

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
    this.parser.parseFile(fileDefinition, path, file -> {
      file.parseFile();
    });
  }

}
