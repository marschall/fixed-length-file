package com.github.marschall.fixedlengthfile;

public final class ByteArrayWritingLineCreator extends AbstractWritingLineCreator<ByteArrayWritingLine> {

  @Override
  protected ByteArrayWritingLine instantiate(SegmentOffsets segmentOffsets, int recordLength) {
    var writingLine = new ByteArrayWritingLine(recordLength);
    writingLine.setSegmentsOffsets(segmentOffsets);
    return writingLine;
  }

}
