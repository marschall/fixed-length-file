package com.github.marschall.fixedlengthfile;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class FileDefinitionFixedLengthFileParser extends AbstractFixedLengthFileParser {
  
  private final FileDefinition fileDefinition;

  public FileDefinitionFixedLengthFileParser(FileDefinition fileDefinition) {
    this.fileDefinition = Objects.requireNonNull(fileDefinition);
  }

  @Override
  protected FileDefinition getFileDefinition(MemorySegment segment) {
    return this.fileDefinition;
  }

}
