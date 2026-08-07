package com.github.marschall.fixedlenghtfile.configuration.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.github.marschall.fixedlenghtfile.configuration.parser.ConfigurationParser.fixDataType;

class ConfigurationParserTests {

  private ConfigurationParser parser;

  @BeforeEach
  void setUp() throws XPathExpressionException {
    this.parser = new ConfigurationParser(new InterfaceVersion("2.67.0"));
  }

  @Test
  void parseReference() throws Exception {
    Path referenceFile = findFileIn(Paths.get("src/test/resources/reference"), "xml");
    assertNotNull(referenceFile);
    this.parser.parse(referenceFile, Set.of("HD", "KT", "TR"));
  }

  private static Path findFileIn(Path basePath, String exentsion) throws IOException {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(basePath, "*." + exentsion)) {
      for (Path path : directoryStream) {
        return path;
      }
    }
    throw new NoSuchElementException("no file with extension: " + exentsion + " found in: " + basePath);
  }

  @Test
  void testFixDataType() {
    // valid ones
    assertSame("CHAR(2)", fixDataType("CHAR(2)"));
    assertSame("NUM(9)", fixDataType("NUM(9)"));
    assertSame("SNUM(13)", fixDataType("SNUM(13)"));

    // invalid ones
    assertEquals("CHAR(18)", fixDataType("CHAR18)"));
    assertEquals("CHAR(50)", fixDataType("CHAR (50)"));
    assertEquals("NUM(2)", fixDataType("NUM2"));
  }

}
