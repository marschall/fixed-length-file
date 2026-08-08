package com.github.marschall.fixedlenghtfile;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;

public interface WritingLine {

  void writeUnsignedInt(BoundIntegerField field, int value);

  void writeUnsignedLong(BoundLongField field, long value);

  void writeString(BoundStringField field, String s);

  void writeSegmentIndicator(BoundSegmentIndicatorField field, SegmentIndicator indicator);

}
