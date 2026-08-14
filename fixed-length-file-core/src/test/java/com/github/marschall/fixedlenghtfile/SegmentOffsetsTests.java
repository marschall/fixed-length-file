package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SegmentOffsetsTests {

  @Test
  void getSegmentOffset() {
    var segmentOffsets = new SegmentOffsets(3);
    segmentOffsets.setSegmentOffset(0, 123);
    segmentOffsets.setSegmentNotPresent(1);
    segmentOffsets.setSegmentIsSpaces(2);

    assertEquals(123, segmentOffsets.getSegmentOffset(0));
    assertEquals(SegmentOffsets.SEGMENT_NOT_PRESENT, segmentOffsets.getSegmentOffset(1));
    assertEquals(SegmentOffsets.SEGMENT_IS_SPACES, segmentOffsets.getSegmentOffset(2));
  }

}
