package com.github.marschall.fixedlengthfile;

public final class CharArrayWritingLineCreator extends AbstractWritingLineCreator<CharArrayWritingLine> {

  @Override
  protected CharArrayWritingLine instantiate(SegmentOffsets segmentOffsets, int recordLength) {
    var writingLine = new CharArrayWritingLine(recordLength);
    writingLine.setSegmentsOffsets(segmentOffsets);
    return writingLine;
  }

}
