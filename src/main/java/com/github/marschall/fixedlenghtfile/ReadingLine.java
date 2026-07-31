package com.github.marschall.fixedlenghtfile;

public interface ReadingLine {

  int readUnsignedIntAt(int offset, int length);

  long readUnsignedLongAt(int offset, int length);

  String readTrimmedStringAt(int offset, int length);
  
  SegmentIndicator readSegmentIndicatorAt(int offset);

}
