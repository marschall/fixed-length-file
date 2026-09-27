package com.github.marschall.fixedlengthfile.configuration.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlengthfile.FieldDefinition;
import com.github.marschall.fixedlengthfile.FileDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;

class InterfaceDefinition267Tests {

  private FileDefinition definition;

  @BeforeEach
  void setUp() {
    this.definition = InterfaceDefinition267.definition();
  }

  @Test
  void hd() {
    var recordDefintion = this.definition.getRecordDefinition("HD");
    FixedLengthRecordDefinition hd = assertInstanceOf(FixedLengthRecordDefinition.class, recordDefintion);
    assertEquals(87, hd.getBaseLength());
    assertEquals(87, hd.getMaximumLength());
  }

  @Test
  void kt() {
    var recordDefintion = this.definition.getRecordDefinition("KT");
    SegmentedRecordDefinition kt = assertInstanceOf(SegmentedRecordDefinition.class, recordDefintion);
    assertEquals(47 + 109 + 741 + 14 + 296 + 35 + 248 + 187 + 526 + 116 + 184 + 355 + 8, kt.getBaseLength());
    assertEquals(4525, kt.getMaximumLength());
    
    List<SegmentDefinition> segmentDefinitions = kt.getSegmentDefinitions();
    assertThat(segmentDefinitions).hasSize(8);
    List<String> segmentIndicatorFields = segmentDefinitions.stream()
        .map(SegmentDefinition::getSegmentIndicatorField)
        .map(FieldDefinition::getName)
        .toList();
    assertEquals(List.of("KT66A", "KT66B", "KT67", "KT68", "KT69", "KT70", "KT71", "KT72"), segmentIndicatorFields);
    int[] segmentIndicatorIndices = segmentDefinitions.stream()
        .mapToInt(SegmentDefinition::getSegmentIndicatorIndex)
        .toArray();
    assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7}, segmentIndicatorIndices);
  }

  @Test
  void tr() {
    var recordDefintion = this.definition.getRecordDefinition("TR");
    FixedLengthRecordDefinition tr = assertInstanceOf(FixedLengthRecordDefinition.class, recordDefintion);
    assertEquals(155, tr.getBaseLength());
    assertEquals(155, tr.getMaximumLength());
  }

}
