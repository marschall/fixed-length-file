package com.github.marschall.fixedlenghtfile;

import java.io.IOException;
import java.io.Reader;

public abstract class BufferedReadingLine implements ReadingLine {

  private final FileDefinition fileDefinition;
  private final StringBuilder stringBuilder;
  private final int maximumPrefixLength;

  public BufferedReadingLine(FileDefinition fileDefinition) {
    this.fileDefinition = fileDefinition;
    this.stringBuilder = new StringBuilder(fileDefinition.getMaximumPrefixLength());
    this.maximumPrefixLength = this.fileDefinition.getMaximumPrefixLength();
  }

  public void initializeFrom(Reader reader) throws IOException {
    // FIXME
    int c = reader.read();
    while (c != -1) {
      this.stringBuilder.append(c);
      c = reader.read();
    }
    
    String prefix = this.stringBuilder.substring(0, this.maximumPrefixLength);
    RecordDefinition recordDefinition = this.fileDefinition.getRecordDefinitionFromPrefix(prefix);
  }

}
