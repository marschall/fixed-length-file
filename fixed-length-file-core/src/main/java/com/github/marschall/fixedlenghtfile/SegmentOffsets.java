package com.github.marschall.fixedlenghtfile;

final class SegmentOffsets {

  static final int SEGMENT_NOT_PRESENT = -1;
  static final int SEGMENT_IS_SPACES = -2;
  
  private final short[] segmentOffsets;

  SegmentOffsets(int segmentCount) {
    this.segmentOffsets = new short[segmentCount];
  }

  void setSegmentOffset(int segmentIndex, int segmentLength) {
    this.segmentOffsets[segmentIndex] = (short) segmentLength;
  }
  
  void setSegmentNotPresent(int segmentIndex) {
    this.segmentOffsets[segmentIndex] = SEGMENT_NOT_PRESENT;
  }
  
  void setSegmentIsSpaces(int segmentIndex) {
    this.segmentOffsets[segmentIndex] = SEGMENT_IS_SPACES;
  }

  int getSegmentOffset(int segmentIndex) {
    return this.segmentOffsets[segmentIndex];
  }
  
  // TODO boolean counts towards record length

}
