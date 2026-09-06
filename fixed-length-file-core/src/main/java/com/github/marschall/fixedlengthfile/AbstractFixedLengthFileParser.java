package com.github.marschall.fixedlengthfile;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.function.Consumer;

public abstract class AbstractFixedLengthFileParser {

  protected AbstractFixedLengthFileParser() {
    super();
  }

  protected abstract FileDefinition getFileDefinition(MemorySegment segment);

  public void parseFile(Path path, Consumer<StatelessFixedLengthFile> callback) throws IOException {
    try (FileChannel channel = FileChannel.open(path, READ)) {
      long fileSize = channel.size();
      FileLock fileLock = channel.lock(0, fileSize, true);
      try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = channel.map(READ_ONLY, 0, fileSize, arena);
        FileDefinition fileDefinition = getFileDefinition(segment);
        parseMemorySegment(fileDefinition, segment, callback);
      } finally {
        fileLock.release();
      }
    }
  }

  static void parseMemorySegment(FileDefinition fileDefinition, MemorySegment segment, Consumer<StatelessFixedLengthFile> callback) {
    var fixedLengthFile = new StatelessFixedLengthFile(fileDefinition, segment);
    callback.accept(fixedLengthFile);
  }

  public StatefulFixedLengthFile parseFile(Path path, Arena arena) throws IOException {
    FileChannel channel = FileChannel.open(path, READ);
    long fileSize = channel.size();
    FileLock fileLock = channel.lock(0, fileSize, true);
    MemorySegment segment = channel.map(READ_ONLY, 0, fileSize, arena);
    FileDefinition fileDefinition = getFileDefinition(segment);
    return new StatefulFixedLengthFile(fileDefinition, segment, () -> {
      fileLock.release();
      channel.close();
    });
  }

}