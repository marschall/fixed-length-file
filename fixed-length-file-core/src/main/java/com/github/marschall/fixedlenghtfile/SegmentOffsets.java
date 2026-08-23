package com.github.marschall.fixedlenghtfile;

import java.util.Arrays;

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
  
  @Override
  public String toString() {
    return Arrays.toString(this.segmentOffsets);
  }
  // TODO boolean counts towards record length

}
