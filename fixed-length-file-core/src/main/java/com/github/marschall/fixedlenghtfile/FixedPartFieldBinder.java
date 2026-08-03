package com.github.marschall.fixedlenghtfile;

import java.util.function.Consumer;

import com.github.marschall.fixedlenghtfile.FieldDefinition.SegmentFieldDefinition;

public interface FixedPartFieldBinder extends LineFieldBinder {

  void defineSegment(SegmentFieldDefinition segmentDefinition, Consumer<SegmentFieldBinder> segmentBinder);

}
