package com.github.marschall.fixedlengthfile;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.marschall.fixedlengthfile.FileInformation.Versioned.Main;
import com.github.marschall.fixedlengthfile.FileInformation.Versioned.Variant;

public final class FixedLengthFileParser {
  
  private static final int MAXIMUM_HEADER_LENGTH = Math.max(87, 149);
  
  private final FileDefinitionRepository fileDefinitionRepository;

  public FixedLengthFileParser(FileDefinitionRepository fileDefinitionRepository) {
    this.fileDefinitionRepository = Objects.requireNonNull(fileDefinitionRepository);
  }

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

  private static void parseMemorySegment(FileDefinition fileDefinition, MemorySegment segment, Consumer<StatelessFixedLengthFile> callback) {
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

  private FileDefinition getFileDefinition(MemorySegment segment) {
    if (segment.byteSize() == 0) {
      throw new IllegalArgumentException("File is empty");
    }
    if (this.fileDefinitionRepository.getFileDefinitionCount() == 1) {
      return this.fileDefinitionRepository.getSoleFileDefinition();
    }
    var fileInformation = parseFileInformation(segment);
    return this.fileDefinitionRepository.getFileDefinition(fileInformation);
  }

  private static FileInformation parseFileInformation(MemorySegment segment) {
    if (segment.byteSize() < MAXIMUM_HEADER_LENGTH) {
      throw new FileFormatException("File to be at least " + MAXIMUM_HEADER_LENGTH);
    }
    byte[] array = segment.asSlice(0, MAXIMUM_HEADER_LENGTH).toArray(ValueLayout.JAVA_BYTE);
    String prefix = new String(array, 0, 2, ISO_8859_1);
    if (!prefix.equals("HD")) {
      throw new FileFormatException("Expected prefix 'HD' but got '" + prefix + "'");
    }
    String recSeqNo = new String(array, 2, 9, ISO_8859_1);
    boolean isMain = recSeqNo.equals("000000000");
    if (isMain) {
      return new Main(toMainVersion(new String(array, 68, 3, ISO_8859_1)));
    } else {
      return new Variant(toVarianVersion(new String(array, 2, 3, ISO_8859_1)));
    }
  }

  static FileDefinition.Version toMainVersion(String version) {
    // 267 -> 2.67
    int major = Integer.parseInt(version, 0, 1, 10);
    int minor = Integer.parseInt(version, 1, 3, 10);
    return FileDefinition.Version.of(major, minor);
  }
  
  static FileDefinition.Version toVarianVersion(String version) {
    // 3.0 -> 3.0
    if (version.charAt(1) != '.') {
      throw new FileFormatException("Expected '.' at second index of " + version);
    }
    int major = Integer.parseInt(version, 0, 1, 10);
    int minor = Integer.parseInt(version, 2, 3, 10);
    return FileDefinition.Version.of(major, minor);
  }

}
