package com.github.marschall.fixedlenghtfile;

import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;

public final class StatefulFixedLengthFile extends FixedLengthFile implements Closeable {

  private final Closeable closeable;
  private long position;

  public StatefulFixedLengthFile(FileDefinition fileDefinition, MemorySegment segment, Closeable closeable) {
    super(fileDefinition, segment);
    this.closeable = Objects.requireNonNull(closeable, "closeable");
    this.position = 0L;
  }

  public ReadingLine nextLineOrNull() {
    if (this.segment.byteSize() == 0) {
      return null;
    }
    if (this.position >= this.segment.byteSize()) {
      return null;
    }
    LineInformation lineInformation = this.preParseLine(this.position);
    RecordDefinition recordDefinition = lineInformation.recordDefinition();
    int recordLength = lineInformation.recordLength();
    MemorySegment lineSegment = this.segment.asSlice(this.position, recordLength);
    Latin1MemorySegmentReadingLine line = switch (recordDefinition) {
      case FixedLengthRecordDefinition _ ->  {
        yield new FixedLatin1MemorySegmentReadingLine(lineSegment);
      }
      case SegmentedRecordDefinition _ -> {
        yield new SegmentedLatin1MemorySegmentReadingLine(lineSegment, lineInformation.segmentOffsets());
      }
    };
    this.position = this.advanceBeyondNewline(this.position + recordLength);
    return line;
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
