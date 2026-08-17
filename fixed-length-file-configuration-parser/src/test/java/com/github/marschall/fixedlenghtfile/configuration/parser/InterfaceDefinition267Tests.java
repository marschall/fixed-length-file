package com.github.marschall.fixedlenghtfile.configuration.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.FileDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;

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
  }
  
  @Test
  void tr() {
    var recordDefintion = this.definition.getRecordDefinition("TR");
    FixedLengthRecordDefinition tr = assertInstanceOf(FixedLengthRecordDefinition.class, recordDefintion);
    assertEquals(155, tr.getBaseLength());
    assertEquals(155, tr.getMaximumLength());
  }

}
