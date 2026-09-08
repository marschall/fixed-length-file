package com.github.marschall.fixedlengthfile.configuration.parser;

import static org.assertj.core.api.Assertions.assertThat;
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

class InterfaceDefinition300Tests {

  private FileDefinition definition;

  @BeforeEach
  void setUp() {
    this.definition = InterfaceDefinition300.definition();
  }

  @Test
  void hd() {
    var recordDefintion = this.definition.getRecordDefinition("HD");
    var hd = assertInstanceOf(FixedLengthRecordDefinition.class, recordDefintion);
    assertEquals(149, hd.getBaseLength());
    assertEquals(149, hd.getMaximumLength());
  }

  @Test
  void d1() {
    var recordDefintion = this.definition.getRecordDefinition("D1");
    var d1 = assertInstanceOf(SegmentedRecordDefinition.class, recordDefintion);
    List<SegmentDefinition> segmentDefinitions = d1.getSegmentDefinitions();
    assertThat(segmentDefinitions).hasSize(4);
    List<String> segmentIndicatorFields = segmentDefinitions.stream()
        .map(SegmentDefinition::getSegmentIndicatorField)
        .map(FieldDefinition::getName)
        .toList();
    assertEquals(List.of("D50", "D51", "D52", "D53"), segmentIndicatorFields);
    assertEquals(1807, d1.getBaseLength()
        // YYNN
        + segmentDefinitions.getFirst().getLength() + segmentDefinitions.get(1).getLength());
    assertEquals(2096, d1.getBaseLength()
        // YYYN
        + segmentDefinitions.getFirst().getLength() + segmentDefinitions.get(1).getLength() + segmentDefinitions.get(1).getLength());
  }

  @Test
  void d3() {
    var recordDefintion = this.definition.getRecordDefinition("D3");
    var d3 = assertInstanceOf(FixedLengthRecordDefinition.class, recordDefintion);
    assertEquals(305, d3.getBaseLength());
    assertEquals(305, d3.getMaximumLength());
  }

  @Test
  void tr() {
    var recordDefintion = this.definition.getRecordDefinition("TR");
    var tr = assertInstanceOf(FixedLengthRecordDefinition.class, recordDefintion);
    assertEquals(227, tr.getBaseLength());
    assertEquals(227, tr.getMaximumLength());
  }

}
