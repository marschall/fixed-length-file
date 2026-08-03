package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;

public interface ReadingLine {

//  int readUnsignedIntAt(int offset, int length);
//
//  long readUnsignedLongAt(int offset, int length);
//
//  String readTrimmedStringAt(int offset, int length);
//  
//  SegmentIndicator readSegmentIndicatorAt(int offset);
  
  int readUnsignedInt(BoundIntegerField field);
  
  long readUnsignedLong(BoundLongField field);
  
  String readTrimmedString(BoundStringField fiel);
  
  SegmentIndicator readSegmentIndicator(BoundSegmentIndicatorField field);
  
  //TODO byte or char
  int getLength();

}
