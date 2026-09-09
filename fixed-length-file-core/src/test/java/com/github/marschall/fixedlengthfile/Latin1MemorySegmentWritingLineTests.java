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

}
