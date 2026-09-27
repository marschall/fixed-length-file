package com.github.marschall.fixedlengthfile.configuration.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.github.marschall.fixedlengthfile.configuration.parser.RecordDefinitionFragment.RecordDefinition;
import com.github.marschall.fixedlengthfile.configuration.parser.RecordDefinitionFragment.SegmentDefinition;

public class ConfigurationParser {

  private final XPath xPath;
  private final XPathExpression idText;
  private final XPathExpression nameText;
  private final XPathExpression descriptionText;
  private final XPathExpression continueNumberingText;
  private final XPathExpression lengthText;
  private final XPathExpression datatypeText;
  private final XPathExpression recordSegmentPath;
  private final XPathExpression fieldPath;
  private final InterfaceVersion currentVersion;

  public ConfigurationParser(InterfaceVersion currentVersion) throws XPathExpressionException {
    this.currentVersion = currentVersion;
    this.xPath = XPathFactory.newInstance().newXPath();
    this.idText = xPath.compile("./id[1]/text()");
    this.nameText = xPath.compile("./name[1]/text()");
    this.descriptionText = xPath.compile("./description[1]/text()");
    this.continueNumberingText = xPath.compile("./continueNumbering[1]/text()");
    this.lengthText = xPath.compile("./length[1]/text()");
    this.datatypeText = xPath.compile("./datatype[1]/text()");
    this.recordSegmentPath = xPath.compile("./recordSegment");
    this.fieldPath = xPath.compile("./fields/field");
  }
  
  public List<RecordDefinition> parse(Path path, Set<String> interestingRecordTypes) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    var document = documentBuilder.parse(path.toFile());
    List<RecordDefinition> recordDefinitions = new ArrayList<>(interestingRecordTypes.size());
    for (Node record : xPath.evaluateExpression("/interface/file[1]/section/record", document, XPathNodes.class)) {
      String recordName = this.nameText.evaluateExpression(record, String.class);
      if (interestingRecordTypes.contains(recordName)) {
        recordDefinitions.add(processSegments(recordName, record));
      }
    }
    return recordDefinitions;
  }

  private RecordDefinition processSegments(String recordName, Node record) throws XPathExpressionException {
    List<Field> fields = new ArrayList<>();
    List<SegmentDefinition> segments = new ArrayList<>();
    List<Field> currentFields = fields;
    boolean isFirstSegment = true;
    for (Node recordSegment : this.recordSegmentPath.evaluateExpression(record, XPathNodes.class)) {
      
      String continueNumbering = this.continueNumberingText.evaluateExpression(recordSegment, String.class);
      boolean isFixedSegment = isFirstSegment || "true".equals(continueNumbering);
      if (!isFixedSegment) {
        if (currentFields != fields) {
          segments.add(new SegmentDefinition(segments.size(), currentFields));
        }
        currentFields = new ArrayList<>();
      }

      for (Node field : this.fieldPath.evaluateExpression(recordSegment, XPathNodes.class)) {
        if (this.isInCurrentVersion(field)) {
          String id = this.idText.evaluateExpression(field, String.class);
          String name = this.nameText.evaluateExpression(field, String.class);
          String description = this.descriptionText.evaluateExpression(field, String.class);
          int length = this.lengthText.evaluateExpression(field, Integer.class);
          DataType dataType = mapDataType(field);
          int offset;
          if (currentFields.isEmpty()) {
            offset = 0;
          } else {
            Field previous = currentFields.getLast();
            offset = previous.offset() + previous.length();
          }
          Field newField = new Field(id, name, offset, length, dataType, description);
          if (!currentFields.isEmpty() && currentFields.getLast().id().equals(id)) {
            // multiple final versions, overwrite the previous one
            currentFields.set(currentFields.size() - 1, newField);
          } else {
            currentFields.add(newField);
          }
        }
      }
      isFirstSegment = false;
    }
    if (currentFields != fields) {
      segments.add(new SegmentDefinition(segments.size(), currentFields));
    }
    return new RecordDefinition(recordName, fields, segments);
  }

  private DataType mapDataType(Node field) throws XPathExpressionException {
    // CHAR(2) NUM(9) SNUM(13)
    String dataTypeString = this.datatypeText.evaluateExpression(field, String.class);
    dataTypeString = fixDataType(dataTypeString);
    if (dataTypeString.startsWith("CHAR")) {
      return DataType.CHAR;
    } else if (dataTypeString.startsWith("NUM")) {
      return DataType.NUM;
    } else if (dataTypeString.startsWith("SNUM")) {
      return DataType.SNUM;
    }
    throw new IllegalArgumentException("unknown data type: " + dataTypeString);
  }
  
  private boolean isInCurrentVersion(Node field) {
    // TODO handle multiple finals
    InterfaceVersion validToVersion = getVersionAttributeValue(field, "validToVersion");
    if (validToVersion != null && validToVersion.compareTo(this.currentVersion) <= 0) {
      return false;
    }
    InterfaceVersion validFromVersion = getVersionAttributeValue(field, "validFromVersion");
    return validFromVersion == null || validFromVersion.compareTo(this.currentVersion) <= 0;
  }

  private InterfaceVersion getVersionAttributeValue(Node node, String attributeValue) {
    NamedNodeMap attributes = node.getAttributes();
    Node attribute = attributes.getNamedItem(attributeValue);
    if (attribute != null) {
      return InterfaceVersion.parse(attribute.getTextContent());
    } else {
      return null;
    }
  }
  
  int extractLengt(String dataType) {
    return Integer.parseInt(dataType, dataType.indexOf('(') + 1, dataType.length() - 1, 10);
  }

  static String fixDataType(String dataType) {
    if (dataType.indexOf(' ') != -1) {
      dataType = dataType.replace(" ", "");
    }
    if (dataType.charAt(dataType.length() - 1) != ')') {
      dataType = dataType + ')';
    }
    if (dataType.indexOf('(') == -1) {
      dataType = insertOpeningBacket(dataType);
    }
    return dataType;
  }

  private static String insertOpeningBacket(String dataType) {
    StringBuilder builder = new StringBuilder(dataType.length() + 1);
    boolean firstNumeric = true;
    for (int i = 0; i < dataType.length(); i++) {
      char c = dataType.charAt(i);
      if (c >= '0' && c <= '9') {
        if (firstNumeric) {
          builder.append('(');
        }
        firstNumeric = false;
      }
      builder.append(c);
    }
    return builder.toString();
  }

}
