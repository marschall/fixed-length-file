package com.github.marschall.fixedlenghtfile;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

final class Latin1MemorySegmentWritingLine implements WritingLine {
  // TODO currently unlimited lenght, could benefit from slice()
  
  private final MemorySegment segment;
  
  private final long start;

  Latin1MemorySegmentWritingLine(MemorySegment segment, long start) {
    this.segment = segment;
    this.start = start;
  }

  @Override
  public void writeUnsignedIntAt(int offset, int length, int value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void writeUnsignedLongAt(int offset, int length, long value) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void writeStringAt(int offset, int length, String s) {
    // REVIEW this.segment.setString will add 0 terminator
    
    // TODO Auto-generated method stub
    
  }

  private void writeCharAt(long index, char c) {
    byte b = (byte) c;
    this.segment.setAtIndex(ValueLayout.JAVA_BYTE, index, b);
  }

}
