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

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.FixedLengthFileParserTests.SampleWithRecordType.F;
import com.github.marschall.fixedlenghtfile.FixedLengthFileParserTests.SampleWithRecordType.H;
import com.github.marschall.fixedlenghtfile.FixedLengthFileParserTests.SampleWithRecordType.R;

class FixedLengthFileParserTests {

  static final class SampleWithRecordType {

    static final class H {

      static final StringFieldDefinition TYPE;

      static final StringFieldDefinition FIELD1;

      static {
        TYPE = new StringFieldDefinition("TYPE", 1, 0);
        FIELD1 = new StringFieldDefinition("FIELD-1", 1, TYPE.getOffset() + TYPE.getLength());
      }

    }

    static final class R {

      static final StringFieldDefinition TYPE;
      static final StringFieldDefinition FIELD2;
      static final UnsignedFieldDefinition FIELD3;
      static final StringFieldDefinition S1;
      static final StringFieldDefinition S2;
      static final StringFieldDefinition S3;
      
      static {
        TYPE = new StringFieldDefinition("TYPE", 1, 0);
        FIELD2 = new StringFieldDefinition("FIELD-2", 3, TYPE.getOffset() + TYPE.getLength());
        FIELD3 = new UnsignedFieldDefinition("FIELD-3", 3, FIELD2.getOffset() + FIELD2.getLength());
        S1 = new StringFieldDefinition("S1", 1, FIELD3.getOffset() + FIELD3.getLength());
        S2 = new StringFieldDefinition("S2", 1, S1.getOffset() + S1.getLength());
        S3 = new StringFieldDefinition("S3", 1, S2.getOffset() + S2.getLength());
      }

      static final class Segment1 {

        static final StringFieldDefinition S1_1 = new StringFieldDefinition("S1-1", 1, 0);

      }

      static final class Segment2 {
        static final StringFieldDefinition S2_1 = new StringFieldDefinition("S2-1", 1, 0);

      }

      static final class Segment3 {

        static final StringFieldDefinition S3_1 = new StringFieldDefinition("S3-1", 1, 0);
      }

    }

    static final class F {

      static final StringFieldDefinition TYPE;

      static final StringFieldDefinition FIELD1;

      static {
        TYPE = new StringFieldDefinition("TYPE", 1, 0);
        FIELD1 = new StringFieldDefinition("FIELD-1", 1, TYPE.getOffset() + TYPE.getLength());
      }

    }

  }

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
          binder.bind(H.TYPE);
          binder.bind(H.FIELD1);
        })
        .defineRecordType("R", binder -> {
          binder.bind(R.TYPE);
          binder.bind(R.FIELD2);
          binder.bind(R.FIELD3);
          binder.defineSegment(R.S1, segmentBinder -> {
            segmentBinder.bind(R.Segment1.S1_1);
          });
          binder.defineSegment(R.S2, segmentBinder -> {
            segmentBinder.bind(R.Segment2.S2_1);
          });
          binder.defineSegment(R.S3, segmentBinder -> {
            segmentBinder.bind(R.Segment3.S3_1);
          });
        })
        .defineRecordType("F", binder -> {
          binder.bind(F.TYPE);
          binder.bind(F.FIELD1);
        })
        .build();

    RecordDefinition headerDefinition = fileDefinition.getRecordDefinition("H");
    assertEquals(2, headerDefinition.getMaximumLength());

    RecordDefinition recordDefinition = fileDefinition.getRecordDefinition("R");
    assertEquals(13, recordDefinition.getMaximumLength());

    RecordDefinition footerDefinition = fileDefinition.getRecordDefinition("F");
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
          assertEquals("H", line.readTrimmedString(H.TYPE), "header type");
          assertEquals("1", line.readTrimmedString(H.FIELD1), "header field 1");
        }
        case "F" -> {
          assertEquals("F", line.readTrimmedString(F.TYPE), "footer type");
          assertEquals("2", line.readTrimmedString(F.FIELD1), "footer field 1");
        }
        case "R" -> {
          assertEquals("R", line.readTrimmedString(R.TYPE), "record type");
          switch (recordNumber) {
          case 1 -> {
            assertSame(SegmentIndicator.PRESENT, R.S1);
            assertSame(SegmentIndicator.PRESENT, R.S2);
            assertSame(SegmentIndicator.PRESENT, R.S3);

            assertEquals("X", line.readTrimmedString(R.S1, R.Segment1.S1_1));
            assertEquals("X", line.readTrimmedString(R.S2, R.Segment2.S2_1));
            assertEquals("X", line.readTrimmedString(R.S3, R.Segment3.S3_1));
          }
          case 2 -> {
            assertSame(SegmentIndicator.ABSENT, R.S1);
            assertSame(SegmentIndicator.ABSENT, R.S2);
            assertSame(SegmentIndicator.ABSENT, R.S3);
          }
          case 3 -> {
            assertSame(SegmentIndicator.SPACES, R.S1);
            assertSame(SegmentIndicator.SPACES, R.S2);
            assertSame(SegmentIndicator.SPACES, R.S3);
          }
          }
        }
        }
      });
    });
    assertEquals(5, expectedRecordNumber.get(), "encounterd records");
  }

}
