package com.github.marschall.fixedlenghtfile.configuration.parser;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Converts the SpecKtbPdf.txt document into KTB enum templates. For a new version copy the right part out of the specification. In the templates for the detail record add the
 * dependencies to the segments. Then copy the classes. !This code is very fragile, every change in the spec leads to an abort! Also the last element of a record type is in the
 * wrong file. Therefore template class files are generated as text files. These files can be diffed with the real java files. Then copy the new elements. This helps especially
 * when lots of new fields are added and lots of typing work can be saved. Also fileName and outputPath are absolute file names in the code. Change it to your needs.
 */
public class KtbSpecConverter {

  private Map<String, String> recordTypes;
  private Map<String, String> recordTypesMap;
  private Map<String, SpecRecord> records;
  private Map<String, SpecSegment> segments;
  private Consumer<SpecField> fieldConsumer;

  public static void main(String[] args) throws Exception {
    KtbSpecConverter generator = new KtbSpecConverter();
    generator.initialiseRecordtypes();
    generator.generateEnums();
  }

  private void generateEnums() throws Exception {
    String fileName = "src/test/resources/reference/SpecKtbPdf.txt";
    Path path = Paths.get(fileName);
    // List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
    List<String> lines = Files.readAllLines(path, ISO_8859_1);

    Path outputPath = null;
    StringBuilder lastLine = new StringBuilder();

    for (String line : lines) {
      if (recordTypes.containsKey(line)) {
        if (outputPath != null) {
          lastLine.append(line);
        }
        SpecRecord record = this.records.get(this.recordTypesMap.get(line));
        this.fieldConsumer = record::addField;
      } else {
        // get the broken lines together
        if (isNewLine(line)) {
          handleLine(lastLine.toString());
          lastLine = new StringBuilder(line);
        } else {
          if (lastLine.length() != 0 && !lastLine.toString().endsWith("_") && !lastLine.toString().endsWith("-")) {
            lastLine.append(" ");
          }
          lastLine.append(line);
        }
      }

    }
    handleLine(lastLine.toString());
    writeOutput();
    System.out.println("finished " + LocalDateTime.now());
  }

  private void writeOutput() throws XMLStreamException, IOException, TransformerFactoryConfigurationError, TransformerException {
    XMLOutputFactory factory = XMLOutputFactory.newInstance();

    Path path = Path.of("src/test/resources/reference/KTB-3000.xml");
    try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(path, WRITE, CREATE, TRUNCATE_EXISTING))) {
      XMLStreamWriter writer = factory.createXMLStreamWriter(output, "UTF-8");
      try {
        writer.writeStartDocument();

        writer.writeStartElement("interface");
        wirteElementWithCharacters(writer, "name", "KTB Interface");

        writer.writeStartElement("file");
        wirteElementWithCharacters(writer, "id", "KTB Interface");

        writeSection(writer, "File Header", List.of(this.records.get("HD")));
        writeSection(writer, "default", List.of(this.records.get("D1"), this.records.get("D3")));
        writeSection(writer, "File Trailer", List.of(this.records.get("TR")));

        writer.writeEndElement(); // file
        writer.writeEndElement(); // interface

        writer.writeEndDocument();
      } finally {
        writer.close();
      }
    }
    prettyPrint(path);
  }

  private void writeSection(XMLStreamWriter writer, String id, List<SpecRecord> records) throws XMLStreamException {
    writer.writeStartElement("section");
    
    wirteElementWithCharacters(writer, "id", id);
    
    for (SpecRecord record : records) {
      writeRecord(writer, record);
    }
    
    writer.writeEndElement(); // section
    
  }

  private void writeRecord(XMLStreamWriter writer, SpecRecord record) throws XMLStreamException {
    writer.writeStartElement("record");

    wirteElementWithCharacters(writer, "name", record.getName());
    
    writer.writeStartElement("recordSegment");
    if (record.hasSegments()) {
      wirteElementWithCharacters(writer, "continueNumbering", "true");
    }
    writer.writeStartElement("fields");
    for (SpecField field : record.getFields()) {
      writeField(writer, field);
    }
    writer.writeEndElement(); // fields
    writer.writeEndElement(); // recordSegment
    
    if (record.hasSegments()) {
      for (SpecSegment segment : record.getSegments()) {
        
      }
    }

    writer.writeEndElement(); // record
  }

  private void writeField(XMLStreamWriter writer, SpecField field) throws XMLStreamException {
    writer.writeStartElement("field");

    wirteElementWithCharacters(writer, "id", field.id());
    wirteElementWithCharacters(writer, "name", field.name());
    wirteElementWithCharacters(writer, "length", Integer.toString(field.length()));
    wirteElementWithCharacters(writer, "datatype", field.dataType().name() + '(' + field.length + ')');

    writer.writeEndElement(); // field
  }
  
  private void wirteElementWithCharacters(XMLStreamWriter writer, String elementName, String value) throws XMLStreamException {
    writer.writeStartElement(elementName);
    writer.writeCharacters(value);
    writer.writeEndElement();
  }

  private void prettyPrint(Path path) throws IOException, TransformerFactoryConfigurationError, TransformerException {
    File original = path.toFile();
    Path renamed = path.resolveSibling(path.getFileName().toString() + ".bak");
    Files.move(path, renamed, REPLACE_EXISTING);
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    // initialize StreamResult with File object to save to file
    var result = new StreamResult(original);
    var source = new StreamSource(renamed.toFile());
    transformer.transform(source, result);
  }

  private boolean isNewLine(String line) {
    return line.matches("^H[0-9]{2}.*|^[0-9]{2,3}.*|^S[0-9]{2}.*|^T[0-9]{2}.*");
  }

  private void handleLine(String line) {

    // exclude some lines like chapters and footer
    boolean isHeader = line.matches("^H[0-9]{2}\\s.*Num.*|H[0-9]{2}\\s.*Char.*");
    boolean isD1 = line.matches("^[0-9]{2,3}\\s.*Num.*|[0-9]{2,3}\\s.*Char.*");
    boolean isD3 = line.matches("^S[0-9]{2}\\s.*Num.*|S[0-9]{2}\\s.*Char.*");
    boolean isTrailer = line.matches("^T[0-9]{2}\\s.*Num.*|T[0-9]{2}\\s.*Char.*");
    if (!isHeader && !isD1 && !isD3 && !isTrailer) {
      // TODO
      return;
    }

    createEnumMember(line, fieldConsumer);


    String segementReference = null;
//    if (isHeader) {
//      SpecRecord record = this.records.get("HD");
//      fieldConsumer = record::addField;
//    }
//    if (isD1 && segementReference.equals("null")) {
//      SpecRecord record = this.records.get("D1");
//      fieldConsumer = record::addField;
//    }
//    if (isD3) {
//      SpecRecord record = this.records.get("D3");
//      fieldConsumer = record::addField;
//    }
//    if (isTrailer) {
//      SpecRecord record = this.records.get("TR");
//      fieldConsumer = record::addField;
//    }
    
//    Consumer<SpecField> fieldConsumer = null;
    if (line.contains("SEGMENT Details für KI-Bank")) {
      segementReference = "E50";
    } else if (line.contains("SEGMENT Rückabwicklung")) {
      segementReference = "E52";
    } else if (line.contains("SEGMENT für Daten in der Bezugswährung")) {
      segementReference = "E51";
    } else if (line.contains("SEGMENT für EMV-basierende Transaktionen")) {
      segementReference = "E53";
    } else if (line.contains("Sammel-Record")) {
      // TODO
//      segementReference = "null";
    }
    if (segementReference != null) {
      SpecSegment segment = this.segments.get(segementReference);
      fieldConsumer = segment::addField;
    }
  }

  private void createEnumMember(String line, Consumer<SpecField> fieldConsumer) {
    // parse the line into the needed elements
    Pattern pattern = Pattern.compile("(^H[0-9]{2}|^[0-9]{2,3}|^S[0-9]{2}|^T[0-9]{2})\\s(\\S*)\\s([0-9]{1,3})\\s[0-9]{1,4}\\s(Num|Char).*");

    Matcher matcher = pattern.matcher(line);

    String id;
    String name;
    String type;
    DataType dataType = null;
    Integer precision;
    if (matcher.matches()) {
      id = matcher.group(1);
      name = matcher.group(2);
      type = matcher.group(4);
      if ("Num".equals(type)) {
        dataType = DataType.NUM;
      } else if ("Char".equals(type)) {
        dataType = DataType.CHAR;
      }
      precision = Integer.valueOf(matcher.group(3));
    } else {
      throw new RuntimeException("how could I just end up here? [" + line + "]");
    }
    // special handling because the D1 (detail) record id has no characters --> enum would not
    // compile
    if (id.matches("^[0-9]{2,3}")) {
      id = "D" + id;
    }
    
    SpecField field = new SpecField(id, name, precision, dataType);
    fieldConsumer.accept(field);

//    return ("  " + "/** ") + id + " " + name + " */\n" + // line 1
//        "  " + // line 2
//        id + "(\"" + name + "\", \"" + type + "\", " + precision + ", " + segementReference + "),\n";
  }

  static final class SpecRecord {
    private final String name;

    private final List<SpecField> fields;

    private final List<SpecSegment> segments;

    SpecRecord(String name, List<SpecSegment> segments) {
      this.name = name;
      this.segments = segments;
      this.fields = new ArrayList<>();
    }
    
    List<SpecSegment> getSegments() {
      return this.segments;
    }

    String getName() {
      return this.name;
    }

    SpecRecord(String name) {
      this(name, List.of());
    }

    void addField(SpecField field) {
      this.fields.add(field);
    }

    List<SpecField> getFields() {
      return this.fields;
    }

    boolean hasSegments() {
      return !this.segments.isEmpty();
    }

    @Override
    public String toString() {
      return "Record: " + this.name;
    }

  }

  static final class SpecSegment {
    private final String name;

    private final List<SpecField> fields;

    SpecSegment(String name) {
      this.name = name;
      this.fields = new ArrayList<>();
    }

    void addField(SpecField field) {
      this.fields.add(field);
    }

  }
  
  record SpecField(String id, String name, int length, DataType dataType) {
    
  }

  private void initialiseRecordtypes() {
    this.recordTypes = new HashMap<>();
    this.recordTypesMap = new HashMap<>();
    this.segments = new HashMap<>();
    this.records = new HashMap<>();
    this.recordTypes.put("10.2 Header-Record", "KtbV2HdEnum");
    this.recordTypes.put("10.3 Detail-Record", "KtbV2D1Enum");
    this.recordTypes.put("10.4 Sammel-Record", "KtbV2D3Enum");
    this.recordTypes.put("10.5 Trailer-Record", "KtbV2TrEnum");
    this.recordTypesMap.put("10.2 Header-Record", "HD");
    this.recordTypesMap.put("10.3 Detail-Record", "D1");
    this.recordTypesMap.put("10.4 Sammel-Record", "D3");
    this.recordTypesMap.put("10.5 Trailer-Record", "TR");

    SpecSegment segment0 = new SpecSegment("E50");
    this.segments.put("E50", segment0);
    SpecSegment segment1 = new SpecSegment("E52");
    this.segments.put("E52", segment1);
    SpecSegment segment2 = new SpecSegment("E51");
    this.segments.put("E51", segment2);
    SpecSegment segment3 = new SpecSegment("E53");
    this.segments.put("E53", segment3);
    
    this.records.put("HD", new SpecRecord("HD"));
    this.records.put("D1", new SpecRecord("D1", List.of(segment0, segment1, segment2, segment3)));
    this.records.put("D3", new SpecRecord("D3"));
    this.records.put("TR", new SpecRecord("TR"));
  }

}
