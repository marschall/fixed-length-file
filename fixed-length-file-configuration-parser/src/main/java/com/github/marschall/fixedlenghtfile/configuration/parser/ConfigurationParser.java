package com.github.marschall.fixedlenghtfile.configuration.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ConfigurationParser {

  private final XPath xPath;
  private final XPathExpression nameText;
  private final XPathExpression continueNumberingText;
  private final XPathExpression idText;
  private final XPathExpression lengthText;
  private final XPathExpression datatypeText;
  private final XPathExpression recordSegmentPath;
  private final XPathExpression fieldPath;

  public ConfigurationParser() throws XPathExpressionException {
    this.xPath = XPathFactory.newInstance().newXPath();
    this.nameText = xPath.compile("name/text()");
    this.continueNumberingText = xPath.compile("continueNumbering/text()");
    this.idText = xPath.compile("id/text()");
    this.lengthText = xPath.compile("length/text()");
    this.datatypeText = xPath.compile("datatype/text()");
    this.recordSegmentPath = xPath.compile("recordSegment");
    this.fieldPath = xPath.compile("fields/field");
  }
  
  public void parse(Path path, Set<String> interestingRecordTypes) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    var document = documentBuilder.parse(path.toFile());
    for (Node record : xPath.evaluateExpression("/interface/file/section/record", document, XPathNodes.class)) {
      String recordName = this.nameText.evaluateExpression(record, String.class);
      if (interestingRecordTypes.contains(recordName)) {
        processSegments(recordName, record);
      }
    }
  }

  private void processSegments(String recordName, Node record) throws XPathExpressionException {
    System.out.println("==");
    System.out.println(recordName);
    boolean isFirst = true;
    for (Node recordSegment : this.recordSegmentPath.evaluateExpression(record, XPathNodes.class)) {
      String continueNumbering = this.continueNumberingText.evaluateExpression(recordSegment, String.class);
      boolean isFixedSegment = isFirst || "true".equals(continueNumbering);
      if (!isFixedSegment) {
        System.out.println("--");
      }
      for (Node field : this.fieldPath.evaluateExpression(recordSegment, XPathNodes.class)) {
        String id = this.idText.evaluateExpression(field, String.class);
        String length = this.lengthText.evaluateExpression(field, String.class);
        // CHAR(2) NUM(9) SNUM(13)
        String datatype = this.datatypeText.evaluateExpression(field, String.class);
        if (!(datatype.startsWith("CHAR(") || datatype.startsWith("NUM(") || datatype.startsWith("SNUM("))) {
          System.out.println("  " + id + " " + length + " " + datatype);
        }
        if (!datatype.endsWith(")")) {
          System.out.println("  " + id + " " + length + " " + datatype);
        }
      }
      isFirst = false;
    }
  }

  static String fixDataType(String datatype) {
    return datatype;
  }

}
