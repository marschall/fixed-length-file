package com.github.marschall.fixedlengthfile;

import java.util.Arrays;

sealed interface SegmentOffsets {

  static final int SEGMENT_NOT_PRESENT = -1;
  static final int SEGMENT_IS_SPACES = -2;


  int getSegmentOffset(int segmentIndex);

  // TODO boolean counts towards record length

  static final class NoSegment implements SegmentOffsets {

    static final SegmentOffsets INSTANCE = new NoSegment();

    private NoSegment() {
      super();
    }

    @Override
    public int getSegmentOffset(int segmentIndex) {
      throw new UnsupportedOperationException("not segmented record");
    }

  }

  static final class ArrayBasedSegmentOffsets implements SegmentOffsets {

    private final short[] segmentOffsets;

    ArrayBasedSegmentOffsets(int segmentCount) {
      this.segmentOffsets = new short[segmentCount];
    }

    void setSegmentOffset(int segmentIndex, int segmentLength) {
      if (segmentIndex > Short.MAX_VALUE) {
        throw new IllegalArgumentException();
      }
      this.segmentOffsets[segmentIndex] = (short) segmentLength;
    }

    void setSegmentNotPresent(int segmentIndex) {
      this.segmentOffsets[segmentIndex] = SEGMENT_NOT_PRESENT;
    }

    void setSegmentIsSpaces(int segmentIndex) {
      this.segmentOffsets[segmentIndex] = SEGMENT_IS_SPACES;
    }

    @Override
    public int getSegmentOffset(int segmentIndex) {
      return this.segmentOffsets[segmentIndex];
    }

    @Override
    public String toString() {
      return Arrays.toString(this.segmentOffsets);
    }
  }

}
