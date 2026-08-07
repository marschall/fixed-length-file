package com.github.marschall.fixedlenghtfile;

import java.util.function.Consumer;

import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;

public interface FixedPartFieldBinder extends LineFieldBinder {

  void defineSegment(StringFieldDefinition segmentIndicatorField, Consumer<SegmentFieldBinder> binderConsumer);

}
