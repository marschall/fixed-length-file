package com.github.marschall.fixedlengthfile.configuration.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlengthfile.configuration.parser.ConfigurationGenerator.SegmentIndicatorFieldIdStrategy;

class ConfigurationGeneratorTests {

  @Test
  void generateTo() throws Exception {
    var interfaceVersion = InterfaceVersion.parse("2.67.0");
    Path referenceFile = Helper.findFileIn(Paths.get("src/test/resources/reference"), "2670", "xml");
    assertNotNull(referenceFile);
    ConfigurationGenerator generator = new ConfigurationGenerator();
    generator.generateTo(interfaceVersion, Set.of("HD", "KT", "TR"), referenceFile, Paths.get("src/main/java"), "com.github.marschall.fixedlengthfile.configuration.parser");
  }

  @Test
  void generateVariant() throws Exception {
    var interfaceVersion = InterfaceVersion.parse("3.0.0");
    Path referenceFile = Helper.findFileIn(Paths.get("src/test/resources/reference"), "3006",  "xml");
    assertNotNull(referenceFile);
    ConfigurationGenerator generator = new ConfigurationGenerator(hardCodedFieldIds());
    generator.generateTo(interfaceVersion, Set.of("HD", "D1", "D3", "TR"), referenceFile, Paths.get("src/main/java"), "com.github.marschall.fixedlengthfile.configuration.parser");
  }

  private static SegmentIndicatorFieldIdStrategy hardCodedFieldIds() {
    return _ -> List.of("D50", "D52", "D51", "D53");
  }

}
