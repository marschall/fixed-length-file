package com.github.marschall.fixedlengthfile.configuration.parser;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlengthfile.FileDefinitionRepository;
import com.github.marschall.fixedlengthfile.RepositoryFixedLengthFileParser;
import com.github.marschall.fixedlengthfile.StatefulFixedLengthFile;

public class RepositoryFixedLengthFileParserTests {

  @Test
  void parse() throws IOException {
    var parser = new RepositoryFixedLengthFileParser(FileDefinitionRepository.builder()
        .addMainFileDefinition(InterfaceDefinition267.definition())
        .addVariantFileDefinition(InterfaceDefinition300.definition())
        .build());
    Path path = Paths.get("/Users/marschall/git/fixed-length-file/fixed-length-file-configuration-parser/src/test/resources/reference/100008.0000.TAP1.SE120720.800000.V260429");
    try (Arena arena = Arena.ofConfined()) {
      try (StatefulFixedLengthFile file = parser.parseFile(path, arena)) {
        
      }
    }
  }

}
