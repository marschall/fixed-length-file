package com.github.marschall.fixedlengthfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.SegmentFieldDefinition;
import com.github.marschall.fixedlengthfile.FixedLengthFileParserTests.SampleWithRecordType.F;
import com.github.marschall.fixedlengthfile.FixedLengthFileParserTests.SampleWithRecordType.H;
import com.github.marschall.fixedlengthfile.FixedLengthFileParserTests.SampleWithRecordType.R;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;

class FixedLengthFileParserTests {

  static final class SampleWithRecordType {

    static FileDefinition definition() {
      return new FileDefinition(List.of(H.definition(), R.definition(), F.definition()));
    }

    static final class H {

      static final StringFieldDefinition TYPE;

      static final StringFieldDefinition FIELD1;

      static {
        TYPE = new StringFieldDefinition("TYPE", 1, 0);
        FIELD1 = new StringFieldDefinition("FIELD-1", 1, TYPE.getOffset() + TYPE.getLength());
      }

      static RecordDefinition definition() {
        return new FixedLengthRecordDefinition("H", List.of(TYPE, FIELD1));
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

      static RecordDefinition definition() {
        return new SegmentedRecordDefinition("R", List.of(TYPE, FIELD2, FIELD3, S1, S2, S3),
            List.of(Segment1.definition(), Segment2.definition(), Segment3.definition()));
      }

      static final class Segment1 {

        static final SegmentFieldDefinition<StringFieldDefinition> S1_1 = new SegmentFieldDefinition<>(0, new StringFieldDefinition("S1-1", 1, 0));

        static SegmentDefinition definition() {
          return new SegmentDefinition(0, R.S1, List.of(S1_1));
        }

      }

      static final class Segment2 {
        
        static final SegmentFieldDefinition<StringFieldDefinition> S2_1 = new SegmentFieldDefinition<>(1, new StringFieldDefinition("S2-1", 1, 0));

        static SegmentDefinition definition() {
          return new SegmentDefinition(1, R.S2, List.of(S2_1));
        }

      }

      static final class Segment3 {

        static final SegmentFieldDefinition<StringFieldDefinition> S3_1 = new SegmentFieldDefinition<>(2, new StringFieldDefinition("S3-1", 1, 0));

        static SegmentDefinition definition() {
          return new SegmentDefinition(2, R.S3, List.of(S3_1));
        }
      }

    }

    static final class F {

      static final StringFieldDefinition TYPE;

      static final StringFieldDefinition FIELD1;

      static {
        TYPE = new StringFieldDefinition("TYPE", 1, 0);
        FIELD1 = new StringFieldDefinition("FIELD-1", 1, TYPE.getOffset() + TYPE.getLength());
      }

      static RecordDefinition definition() {
        return new FixedLengthRecordDefinition("F", List.of(TYPE, FIELD1));
      }

    }

  }

  @Test
  void sampleWithRecordTypes() throws IOException {
    Path path = Path.of("src/test/resources/sample_with_record_types.txt");

    FileDefinition fileDefinition = SampleWithRecordType.definition();

    RecordDefinition headerDefinition = fileDefinition.getRecordDefinition("H");
    assertEquals(2, headerDefinition.getMaximumLength());

    RecordDefinition recordDefinition = fileDefinition.getRecordDefinition("R");
    assertEquals(13, recordDefinition.getMaximumLength());

    RecordDefinition footerDefinition = fileDefinition.getRecordDefinition("F");
    assertEquals(2, footerDefinition.getMaximumLength());

    AtomicInteger expectedRecordNumber = new AtomicInteger(0);
    List<String> expectedRecordTypes = List.of("H", "R", "R", "R", "F");
    List<Integer> expectedLengths = List.of(2, 13, 10, 13, 2);
    FixedLengthFileParser.parseFile(fileDefinition, path, file -> {
      file.parseLines((recordType, recordNumber, line) -> {
        assertEquals(expectedRecordNumber.getAndIncrement(), recordNumber, "record number");
        assertEquals(expectedRecordTypes.get(recordNumber), recordType, "record type");
        assertEquals(expectedLengths.get(recordNumber), line.getLength(), "line length");
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
            assertSame(SegmentIndicator.PRESENT, line.readSegmentIndicator(R.S1));
            assertSame(SegmentIndicator.PRESENT, line.readSegmentIndicator(R.S2));
            assertSame(SegmentIndicator.PRESENT, line.readSegmentIndicator(R.S3));

            assertEquals("X", line.readTrimmedString(R.Segment1.S1_1));
            assertEquals("Y", line.readTrimmedString(R.Segment2.S2_1));
            assertEquals("Z", line.readTrimmedString(R.Segment3.S3_1));
          }
          case 2 -> {
            assertSame(SegmentIndicator.ABSENT, line.readSegmentIndicator(R.S1));
            assertSame(SegmentIndicator.ABSENT, line.readSegmentIndicator(R.S2));
            assertSame(SegmentIndicator.ABSENT, line.readSegmentIndicator(R.S3));
          }
          case 3 -> {
            assertSame(SegmentIndicator.SPACES, line.readSegmentIndicator(R.S1));
            assertSame(SegmentIndicator.SPACES, line.readSegmentIndicator(R.S2));
            assertSame(SegmentIndicator.SPACES, line.readSegmentIndicator(R.S3));

            assertEquals("", line.readTrimmedString(R.Segment1.S1_1));
            assertEquals("", line.readTrimmedString(R.Segment2.S2_1));
            assertEquals("", line.readTrimmedString(R.Segment3.S3_1));
          }
          }
        }
        }
      });
    });
    assertEquals(5, expectedRecordNumber.get(), "encounterd records");
  }

}
