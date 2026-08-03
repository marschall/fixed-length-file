package com.github.marschall.fixedlenghtfile;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;

class Latin1MemorySegmentWritingLineTests {

  @Test
  void writeFirstLine() throws IOException {
    FieldBinder binder = new FieldBinder();
    BoundStringField field1 = binder.bind(FieldDefinitions.FIELD1);
    BoundStringField field2 = binder.bind(FieldDefinitions.FIELD2);
    BoundStringField field3 = binder.bind(FieldDefinitions.FIELD3);
    BoundIntegerField field6 = binder.bind(FieldDefinitions.FIELD6);
    BoundIntegerField field7 = binder.bind(FieldDefinitions.FIELD7);

    byte[] target = new byte[binder.getLength()];
    MemorySegment segment = MemorySegment.ofArray(target);
    var line = new Latin1MemorySegmentWritingLine(segment, 0L);

    line.writeString(field1, "Fi\u00E9ld1");
    line.writeString(field2, "Fi\u00E9ld");
    line.writeString(field3, null);
    line.writeUnsignedInt(field6, 12);
    line.writeUnsignedInt(field7, 3);

    assertEquals("Fi\u00E9ld1Fi\u00E9ld       1203", new String(target, ISO_8859_1));
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
