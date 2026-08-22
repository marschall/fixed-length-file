package com.github.marschall.fixedlenghtfile.configuration.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.EmptyFileGenerator;

@Disabled
class EmptyFileGeneratorTests {
  
  private EmptyFileGenerator generator;

  @BeforeEach
  void setUp() {
    this.generator = new EmptyFileGenerator(InterfaceDefinition267.definition());
  }

  @Test
  void generateFile() throws IOException {
    Path output = Paths.get("src/test/resources/reference/empty.kt");
    this.generator.generateFile(output, 1000);
  }

}
