package com.github.marschall.fixedlenghtfile;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;
import com.github.marschall.fixedlenghtfile.BoundField.UnsignedIntegerFieldDefinition;

class FixedLengthReadingTests {

  @Test
  void test() throws IOException {
    FieldBinder binder = new FieldBinder();
    BoundStringField field1 = binder.bind(FieldDefinitions.FIELD1);
    UnsignedIntegerFieldDefinition field2 = binder.bind(FieldDefinitions.FIELD2);

    try (FileChannel channel = FileChannel.open(Path.of("src/test/resources/sample.txt"), READ)) {
      long fileSize = channel.size();
      FileLock fileLock = channel.lock(0, fileSize, true);
      try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = channel.map(READ_ONLY, 0, fileSize, arena);
        var line = new Latin1MemorySegmentLine(segment, 0L);
        assertEquals("Field1", field1.readTrimmedStringAt(line));
        assertEquals(12, field2.readUnsignedIntAt(line));
      } finally {
        fileLock.release();
      }
    }
  }

}
