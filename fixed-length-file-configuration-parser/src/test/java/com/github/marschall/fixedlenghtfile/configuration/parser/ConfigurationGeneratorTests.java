package com.github.marschall.fixedlenghtfile.configuration.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ConfigurationGeneratorTests {

  @Test
  void generateTo() throws Exception {
    var interfaceVersion = InterfaceVersion.parse("2.67.0");
    Path referenceFile = Helper.findFileIn(Paths.get("src/test/resources/reference"), "xml");
    assertNotNull(referenceFile);
    ConfigurationGenerator generator = new ConfigurationGenerator();
    generator.generateTo(interfaceVersion, Set.of("HD", "KT", "TR"), referenceFile, Paths.get("src/main/java"), "com.github.marschall.fixedlenghtfile.configuration.parser");
  }

}
