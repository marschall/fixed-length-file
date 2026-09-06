package com.github.marschall.fixedlengthfile.configuration.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlengthfile.FileDefinition;
import com.github.marschall.fixedlengthfile.FileDefinitionRepository;
import com.github.marschall.fixedlengthfile.FixedLengthFileParser;
import com.github.marschall.fixedlengthfile.StatefulFixedLengthFile;
import com.github.marschall.fixedlengthfile.StatefulFixedLengthFile.LineLocator;

class EmptyFileGeneratorTests {

  private EmptyFileGenerator generator;
  private FixedLengthFileParser parser;
  private Path path;

  @BeforeEach
  void setUp() {
    FileDefinition definition = InterfaceDefinition267.definition();
    this.parser = new FixedLengthFileParser(FileDefinitionRepository.builder()
        .addMainFileDefinition(definition)
        .build());
    this.path = Paths.get("src/test/resources/reference/empty.kt");
    this.generator = new EmptyFileGenerator(definition);
  }

  @Disabled
  @Test
  void generateFile() throws IOException {
    Path output = this.path;
    this.generator.generateFile(output, 1_000);
  }

  @Test 
  void parseFile() throws IOException {
    try (Arena arena = Arena.ofConfined()) {
      StatefulFixedLengthFile file = this.parser.parseFile(this.path, arena);
      List<LineLocator> locators = file.preparseFile("KT");
      assertThat(locators).hasSize(1000);
    }
  }

  @Test
  void toInterfaceVersion() {
    assertEquals("267", EmptyFileGenerator.toInterfaceVersion(FileDefinition.Version.of(2, 67)));
    assertEquals("207", EmptyFileGenerator.toInterfaceVersion(FileDefinition.Version.of(2, 7)));
  }

}
