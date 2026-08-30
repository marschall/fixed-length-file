package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;

public final class StatelessFixedLengthFile extends FixedLengthFile {

  public StatelessFixedLengthFile(FileDefinition fileDefinition, MemorySegment segment) {
    super(fileDefinition, segment);
  }

  public void parseLines(LineConsumer consumer) {
    if (this.segment.byteSize() == 0) {
      return;
    }
    long position = 0;
    int recordNumber = 0;
    while (position < this.segment.byteSize()) {
      LineInformation lineInformation = this.preParseLine(position);
      RecordDefinition recordDefinition = lineInformation.recordDefinition();
      ReadingLine line = asLine(position, lineInformation, recordDefinition);
      consumer.accept(recordDefinition.getType(), recordNumber, line);
      recordNumber += 1;
      position = this.advanceBeyondNewline(position + lineInformation.recordLength());
    }
  }

  protected ReadingLine readLine(long lineStart) {
    LineInformation lineInformation = this.preParseLine(lineStart);
    int recordLength = lineInformation.recordLength();
    MemorySegment lineSegment = this.segment.asSlice(lineStart, recordLength);
    return new Latin1MemorySegmentReadingLine(lineSegment, lineInformation.segmentOffsets());
  }

}
