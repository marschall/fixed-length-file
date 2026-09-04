package com.github.marschall.fixedlengthfile;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FileDefinition.Version;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;

class Latin1MemorySegmentWritingLineTests {

  static final class R {

//    static final StringFieldDefinition TYPE;
    static final StringFieldDefinition FIELD1;
    static final StringFieldDefinition FIELD2;
    static final StringFieldDefinition FIELD3;
    static final UnsignedFieldDefinition FIELD6;
    static final UnsignedFieldDefinition FIELD7;

    static {
//      TYPE = new StringFieldDefinition("TYPE", 1, 0);
      FIELD1 = new StringFieldDefinition("FIELD-1", 6, 0);
      FIELD2 = new StringFieldDefinition("FIELD-2", 6, FIELD1.getOffset() + FIELD1.getLength());
      FIELD3 = new StringFieldDefinition("FIELD-3", 6, FIELD2.getOffset() + FIELD1.getLength());
      FIELD6 = new UnsignedFieldDefinition("FIELD-6", 2, FIELD3.getOffset() + FIELD3.getLength());
      FIELD7 = new UnsignedFieldDefinition("FIELD-7", 2, FIELD6.getOffset() + FIELD6.getLength());
    }

    static RecordDefinition definition() {
      return new FixedLengthRecordDefinition("R", List.of(FIELD1, FIELD2, FIELD3, FIELD6, FIELD7));
    }

  }

  @Test
  void writeFirstLine() throws IOException {
    FileDefinition fileDefinition = new FileDefinition(Version.of(1, 0), List.of(R.definition()));

    var recordDefinition = fileDefinition.getRecordDefinition("R");

    byte[] target = new byte[recordDefinition.getMaximumLength()];
    MemorySegment segment = MemorySegment.ofArray(target);
    var line = new Latin1MemorySegmentWritingLine(segment);

    line.writeString(R.FIELD1, "Fi\u00E9ld1");
    line.writeString(R.FIELD2, "Fi\u00E9ld");
    line.writeString(R.FIELD3, null);
    line.writeUnsignedInt(R.FIELD6, 12);
    line.writeUnsignedInt(R.FIELD7, 3);

    assertEquals("Fi\u00E9ld1Fi\u00E9ld       1203", new String(target, ISO_8859_1));
  }

  @Test
  void writeEmptyLine() throws IOException {
    FileDefinition fileDefinition = new FileDefinition(Version.of(1, 0), List.of(R.definition()));
    
    var recordDefinition = fileDefinition.getRecordDefinition("R");
    
    byte[] target = new byte[recordDefinition.getMaximumLength()];
    MemorySegment segment = MemorySegment.ofArray(target);
    var line = new Latin1MemorySegmentWritingLine(segment);
    
    line.writeNoValue(R.FIELD1);
    line.writeNoValue(R.FIELD2);
    line.writeNoValue(R.FIELD3);
    line.writeNoValue(R.FIELD6);
    line.writeNoValue(R.FIELD7);
    
    assertEquals("                  0000", new String(target, ISO_8859_1));
  }

  @Test
  void digits() {
    assertEquals(1, Latin1MemorySegmentWritingLine.digits(0));
    assertEquals(1, Latin1MemorySegmentWritingLine.digits(1));
    assertEquals(1, Latin1MemorySegmentWritingLine.digits(9));

    assertEquals(2, Latin1MemorySegmentWritingLine.digits(10));
    assertEquals(2, Latin1MemorySegmentWritingLine.digits(99));

    assertEquals(3, Latin1MemorySegmentWritingLine.digits(100));
    assertEquals(3, Latin1MemorySegmentWritingLine.digits(101));
    assertEquals(3, Latin1MemorySegmentWritingLine.digits(999));

    assertEquals(4, Latin1MemorySegmentWritingLine.digits(1000));
    assertEquals(4, Latin1MemorySegmentWritingLine.digits(1001));
    assertEquals(4, Latin1MemorySegmentWritingLine.digits(9999));

    assertEquals(7, Latin1MemorySegmentWritingLine.digits(1_000_000));
    assertEquals(7, Latin1MemorySegmentWritingLine.digits(9_999_999));

    assertEquals(8, Latin1MemorySegmentWritingLine.digits(10_000_000));
    assertEquals(8, Latin1MemorySegmentWritingLine.digits(99_999_999));

    assertEquals(9, Latin1MemorySegmentWritingLine.digits(100_000_000));
    assertEquals(9, Latin1MemorySegmentWritingLine.digits(900_000_000));
    assertEquals(9, Latin1MemorySegmentWritingLine.digits(Integer.MAX_VALUE));
  }

}
