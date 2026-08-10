package com.github.marschall.fixedlenghtfile.configuration.parser;

import static com.github.marschall.fixedlenghtfile.configuration.parser.ConfigurationParser.fixDataType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.marschall.fixedlenghtfile.configuration.parser.RecordDefinitionFragment.RecordDefinition;
import com.github.marschall.fixedlenghtfile.configuration.parser.RecordDefinitionFragment.SegmentDefinition;

class ConfigurationParserTests {

  private ConfigurationParser parser;

  @BeforeEach
  void setUp() throws XPathExpressionException {
    this.parser = new ConfigurationParser(InterfaceVersion.parse("2.67.0"));
  }

  @Test
  void parseReference() throws Exception {
    Path referenceFile = findFileIn(Paths.get("src/test/resources/reference"), "xml");
    assertNotNull(referenceFile);
    List<RecordDefinition> recordDefinitions = this.parser.parse(referenceFile, Set.of("HD", "KT", "TR"));
    assertThat(recordDefinitions).hasSize(3);

    RecordDefinition hd = recordDefinitions.getFirst();
    assertThat(hd.hasSegments()).isFalse();
    assertEquals("HD", hd.getName());
    assertEquals(87, hd.getLengthOfFields());

    RecordDefinition kt = recordDefinitions.get(1);
    assertEquals("KT", kt.getName());
    assertThat(kt.hasSegments()).isTrue();
    // FIXME
    assertEquals(2866, kt.getLengthOfFields());
    List<SegmentDefinition> segments = kt.getSegments();
    assertThat(segments).hasSize(8);
    assertEquals(70, segments.getFirst().getLengthOfFields());
    assertEquals(61, segments.get(1).getLengthOfFields());
    assertEquals(449, segments.get(2).getLengthOfFields());
    assertEquals(107, segments.get(3).getLengthOfFields());
    assertEquals(73, segments.get(4).getLengthOfFields());
    assertEquals(111, segments.get(5).getLengthOfFields());
    assertEquals(368, segments.get(6).getLengthOfFields());
    assertEquals(420, segments.get(7).getLengthOfFields());

    RecordDefinition tr = recordDefinitions.get(2);
    assertEquals("TR", tr.getName());
    assertThat(tr.hasSegments()).isFalse();
    assertEquals(155, tr.getLengthOfFields());
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
