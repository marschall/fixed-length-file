package com.github.marschall.fixedlenghtfile;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;
import com.github.marschall.fixedlenghtfile.BoundField.UnsignedIntegerFieldDefinition;

class Latin1MemorySegmentReadingLineTests {

  @Test
  void readFirstLine() throws IOException {
    FieldBinder binder = new FieldBinder();
    BoundStringField field1 = binder.bind(FieldDefinitions.FIELD1);
    BoundStringField field2 = binder.bind(FieldDefinitions.FIELD2);
    BoundStringField field3 = binder.bind(FieldDefinitions.FIELD3);
    BoundStringField field4 = binder.bind(FieldDefinitions.FIELD4);
    BoundStringField field5 = binder.bind(FieldDefinitions.FIELD5);
    UnsignedIntegerFieldDefinition field6 = binder.bind(FieldDefinitions.FIELD6);

    try (FileChannel channel = FileChannel.open(Path.of("src/test/resources/sample.txt"), READ)) {
      long fileSize = channel.size();
      FileLock fileLock = channel.lock(0, fileSize, true);
      try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = channel.map(READ_ONLY, 0, fileSize, arena);
        var line = new Latin1MemorySegmentReadingLine(segment, 0L);
        assertEquals("Fi\u00E9ld1", field1.readTrimmedStringFrom(line));
        assertEquals("Fi\u00E9ld", field2.readTrimmedStringFrom(line));
        assertEquals("i\u00E9ld3", field3.readTrimmedStringFrom(line));
        assertEquals("\u00E9l", field4.readTrimmedStringFrom(line));
        assertSame("", field5.readTrimmedStringFrom(line));
        assertEquals(12, field6.readUnsignedIntFrom(line));
      } finally {
        fileLock.release();
      }
    }
  }

  @Test
  void readSecondLine() throws IOException {
    FieldBinder binder = new FieldBinder();
    BoundStringField field1 = binder.bind(FieldDefinitions.FIELD1);
    BoundStringField field2 = binder.bind(FieldDefinitions.FIELD2);
    BoundStringField field3 = binder.bind(FieldDefinitions.FIELD3);
    BoundStringField field4 = binder.bind(FieldDefinitions.FIELD4);
    BoundStringField field5 = binder.bind(FieldDefinitions.FIELD5);
    UnsignedIntegerFieldDefinition field6 = binder.bind(FieldDefinitions.FIELD6);

    try (FileChannel channel = FileChannel.open(Path.of("src/test/resources/sample.txt"), READ)) {
      long fileSize = channel.size();
      FileLock fileLock = channel.lock(0, fileSize, true);
      try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = channel.map(READ_ONLY, 0, fileSize, arena);
        var line = new Latin1MemorySegmentReadingLine(segment, 33L);
        assertEquals("Fi\u00E9ld2", field1.readTrimmedStringFrom(line));
        assertEquals("Fi\u00E9l", field2.readTrimmedStringFrom(line));
        assertEquals("\u00E9ld3", field3.readTrimmedStringFrom(line));
        assertEquals("\u00E9ld", field4.readTrimmedStringFrom(line));
        assertSame("", field5.readTrimmedStringFrom(line));
        assertEquals(34, field6.readUnsignedIntFrom(line));
      } finally {
        fileLock.release();
      }
    }
  }

}
