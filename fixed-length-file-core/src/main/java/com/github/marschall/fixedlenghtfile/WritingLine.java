package com.github.marschall.fixedlenghtfile;

public interface WritingLine {

  void writeUnsignedIntAt(int offset, int length, int value);

  void writeUnsignedLongAt(int offset, int length, long value);

  void writeStringAt(int offset, int length, String s);
  
  void writeSegmentIndicatorAt(int offset, SegmentIndicator indicator);

}
