package com.github.marschall.fixedlenghtfile;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class FixedLengthFileParser {

  public void parseFile(FileDefinition fileDefinition, Path path, Consumer<FixedLengthFile> callback) throws IOException {
    try (FileChannel channel = FileChannel.open(path, READ)) {
      long fileSize = channel.size();
      FileLock fileLock = channel.lock(0, fileSize, true);
      try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = channel.map(READ_ONLY, 0, fileSize, arena);
        parseMemorySegment(fileDefinition, segment, callback);
      } finally {
        fileLock.release();
      }
    }
  }

  public void parseMemorySegment(FileDefinition fileDefinition, MemorySegment segment, Consumer<FixedLengthFile> callback) {
    FixedLengthFile fixedLengthFile = new FixedLengthFile(fileDefinition, segment);
    callback.accept(fixedLengthFile);
  }

}
