package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;

public interface WritingLine {

  void writeUnsignedInt(BoundIntegerField field, int value);

  void writeUnsignedLong(BoundLongField field, long value);

  void writeString(BoundStringField field, String s);

  void writeSegmentIndicator(BoundSegmentIndicatorField field, SegmentIndicator indicator);

}
