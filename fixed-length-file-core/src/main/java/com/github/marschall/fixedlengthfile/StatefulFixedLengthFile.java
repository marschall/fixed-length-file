package com.github.marschall.fixedlengthfile;

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
      if (lineInformation.recordDefinition().getType().equals(recordType)) {
        locators.add(new LineLocator(localPosition, lineInformation));
      }
      localPosition = this.advanceBeyondNewline(localPosition + recordLength);
    }
    return locators;
  }

  public ReadingLine readLine(LineLocator locator) {
    LineInformation lineInformation = locator.lineInformation;
    long lineStart = locator.lineStart;
    return asLine(lineStart, lineInformation);
  }

  public static final class LineLocator {

    private final long lineStart;
    private final LineInformation lineInformation;

    LineLocator(long lineStart, LineInformation lineInformation) {
      this.lineStart = lineStart;
      this.lineInformation = Objects.requireNonNull(lineInformation, "lineInformation");
    }

    @Override
    public String toString() {
      return "start: " + this.lineStart + " line: " + this.lineInformation;
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
    return asLine(lineStart, lineInformation);
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
