package com.github.marschall.fixedlenghtfile;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class StatefulFixedLengthFile extends FixedLengthFile implements Closeable {

  private final Closeable closeable;
  private long position;

  public StatefulFixedLengthFile(FileDefinition fileDefinition, MemorySegment segment, Closeable closeable) {
    super(fileDefinition, segment);
    this.closeable = Objects.requireNonNull(closeable, "closeable");
    this.position = 0L;
  }

  public List<LineLocator> preparseFile(String recordType) {
    if (this.segment.byteSize() == 0) {
      return List.of();
    }
    long localPosition = 0L;
    List<LineLocator> locators = new ArrayList<>();
    while (localPosition < this.segment.byteSize()) {
      LineInformation lineInformation = this.preParseLine(localPosition);
      int recordLength = lineInformation.recordLength();
      locators.add(new LineLocator(localPosition, lineInformation));
      this.position = this.advanceBeyondNewline(this.position + recordLength);
    }
    return locators;
  }
  
  public ReadingLine readLine(LineLocator locator) {
    LineInformation lineInformation = locator.lineInformation;
    RecordDefinition recordDefinition = lineInformation.recordDefinition();
    long lineStart = locator.lineStart;
    return asLine(lineStart, lineInformation, recordDefinition);
  }

  public static final class LineLocator {

    private final long lineStart;
    private final LineInformation lineInformation;

    LineLocator(long lineStart, LineInformation lineInformation) {
      this.lineStart = lineStart;
      this.lineInformation = Objects.requireNonNull(lineInformation, "lineInformation");
    }

  }

  public ReadingLine nextLineOrNull() {
    if (this.segment.byteSize() == 0) {
      return null;
    }
    if (this.position >= this.segment.byteSize()) {
      return null;
    }
    ReadingLine line = this.readLine(this.position);
    this.position = this.advanceBeyondNewline(this.position + line.getLength());
    return line;
  }

  private ReadingLine readLine(long lineStart) {
    LineInformation lineInformation = this.preParseLine(lineStart);
    RecordDefinition recordDefinition = lineInformation.recordDefinition();
    return asLine(lineStart, lineInformation, recordDefinition);
  }

  @Override
  public void close() throws IOException {
    this.closeable.close();
  }

  public long getCurrentPosition() {
    return this.position;
  }

  public void setCurrentPosition(long position) {
    if (position < 0) {
      throw new IllegalArgumentException();
    }
    this.position = position;
  }

}
