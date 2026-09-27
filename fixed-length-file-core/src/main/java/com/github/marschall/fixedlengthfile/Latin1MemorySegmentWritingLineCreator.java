package com.github.marschall.fixedlengthfile;

import java.lang.foreign.MemorySegment;
import java.util.Objects;

public final class Latin1MemorySegmentWritingLineCreator
    extends AbstractWritingLineCreator<Latin1MemorySegmentWritingLine> {
  
  private final MemorySegment segment;
  private long nextOffset;

  public Latin1MemorySegmentWritingLineCreator(MemorySegment segment) {
    this.segment = Objects.requireNonNull(segment, "segment");
    this.nextOffset = 0L;
  }

  @Override
  protected Latin1MemorySegmentWritingLine instantiate(SegmentOffsets segmentOffsets, int recordLength) {
    MemorySegment lineSegment = this.segment.asSlice(this.nextOffset, recordLength);
    var line = new Latin1MemorySegmentWritingLine(lineSegment);
    this.nextOffset += recordLength;
    this.nextOffset += 2; // CR LF
    return line;
  }

}
