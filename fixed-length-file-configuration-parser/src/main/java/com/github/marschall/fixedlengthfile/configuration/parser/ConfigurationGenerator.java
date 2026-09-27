package com.github.marschall.fixedlengthfile.configuration.parser;

import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.github.marschall.fixedlengthfile.FileDefinition.Version;
import com.github.marschall.fixedlengthfile.configuration.parser.RecordDefinitionFragment.RecordDefinition;
import com.github.marschall.fixedlengthfile.configuration.parser.RecordDefinitionFragment.SegmentDefinition;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

public class ConfigurationGenerator {
  
  private static final ClassName FIELD_DEFINITION = ClassName.get("com.github.marschall.fixedlengthfile", "FieldDefinition");
  private static final ClassName SIGNED_FIELD_DEFINITION = FIELD_DEFINITION.nestedClass("OffsetFieldDefinition").nestedClass("SignedFieldDefinition");
  private static final ClassName UNSIGNED_FIELD_DEFINITION = FIELD_DEFINITION.nestedClass("OffsetFieldDefinition").nestedClass("UnsignedFieldDefinition");
  private static final ClassName STRING_FIELD_DEFINITION = FIELD_DEFINITION.nestedClass("OffsetFieldDefinition").nestedClass("StringFieldDefinition");
  private static final ClassName SEGMENT_FIELD_DEFINITION = FIELD_DEFINITION.nestedClass("SegmentFieldDefinition");
  
  private static final ClassName FILE_DEFINITION = ClassName.get("com.github.marschall.fixedlengthfile", "FileDefinition");
  private static final ClassName RECORD_DEFINITION = ClassName.get("com.github.marschall.fixedlengthfile", "RecordDefinition");
  private static final ClassName FIXED_LENGTH_RECORD_DEFINITION = RECORD_DEFINITION.nestedClass("FixedLengthRecordDefinition");
  private static final ClassName SEGMENTED_RECORD_DEFINITION = RECORD_DEFINITION.nestedClass("SegmentedRecordDefinition");
  private static final ClassName SEGMENT_DEFINITION = RECORD_DEFINITION.nestedClass("SegmentDefinition");
  private static final ClassName FILE_DEFINITION_VERSION = FILE_DEFINITION.nestedClass("Version");
  private static final ClassName LIST = ClassName.get("java.util", "List");

  private final SegmentIndicatorFieldIdStrategy segmentIndicatorFieldIdStrategy;

  public ConfigurationGenerator(SegmentIndicatorFieldIdStrategy segmentIndicatorFieldIdStrategy) {
    this.segmentIndicatorFieldIdStrategy = Objects.requireNonNull(segmentIndicatorFieldIdStrategy, "segmentIndicatorFieldIdStrategy");
  }
  
  public ConfigurationGenerator() {
    this(SegmentIndicatorFieldIdStrategy.trailingFieldIds());
  }

  public void generateTo(InterfaceVersion currentVersion, Set<String> interestingRecordTypes, Path interfacePath, Path outputDirectory, String packageName)
      throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    ConfigurationParser parser = new ConfigurationParser(currentVersion);
    List<RecordDefinition> recordDefinitions = parser.parse(interfacePath, interestingRecordTypes);
    String className = "InterfaceDefinition" + currentVersion.toInterfaceString();
    generate(currentVersion, recordDefinitions, outputDirectory, packageName, className);
  }

  private void generate(InterfaceVersion currentVersion, List<RecordDefinition> recordDefintions, Path outputDirectory, String packageName, String className) throws IOException {
    // public final class packageName.className
    TypeSpec.Builder constantContainerBuilder = TypeSpec.classBuilder(ClassName.get(packageName, className))
        .addModifiers(PUBLIC, FINAL);

    for (RecordDefinition recordDefinition : recordDefintions) {
      ClassName interfaceDefinitionClassName = ClassName.get(packageName, className, recordDefinition.getName());
      TypeSpec.Builder recordSpecBuilder = TypeSpec.classBuilder(interfaceDefinitionClassName)
              .addModifiers(PUBLIC, STATIC, FINAL);
      for (Field field : recordDefinition.getFields()) {
        FieldSpec fieldSpec = buildFieldSpec(field);
        recordSpecBuilder.addField(fieldSpec);
      }
      addRecordDefinitionMethod(recordSpecBuilder, recordDefinition);
      
      if (recordDefinition.hasSegments()) {
        addSegments(recordDefinition, interfaceDefinitionClassName, recordSpecBuilder);
      }
      constantContainerBuilder.addType(recordSpecBuilder.build());
    }
    addFileDefinitionMethod(currentVersion, constantContainerBuilder, recordDefintions);

    JavaFile javaFile = JavaFile.builder(packageName, constantContainerBuilder.build())
        .build();

    javaFile.writeToPath(outputDirectory);
  }

  private void addRecordDefinitionMethod(TypeSpec.Builder recordSpecBuilder, RecordDefinition recordDefinition) {
    // public static RecordDefinition definition()
    MethodSpec.Builder definitionBuilder = MethodSpec.methodBuilder("definition")
        .returns(RECORD_DEFINITION)
        .addModifiers(PUBLIC, STATIC);
    String fieldList = recordDefinition.getFields().stream()
        .map(Field::id)
        .collect(joining(", "));
    if (recordDefinition.hasSegments()) {
      String segmentDefinitions = IntStream.rangeClosed(1, recordDefinition.getSegments().size())
          .mapToObj(i -> "Segment" + i + ".definition()")
          .collect(joining(", "));
      // return new SegmentedRecordDefinition(recordType, List.of(fixedFields), List.of(fixedFields), List.of(segmentDefinitions))
      definitionBuilder.addStatement("return new $T($S, $T.of(" + fieldList + "), $T.of(" + segmentDefinitions + "))", SEGMENTED_RECORD_DEFINITION, recordDefinition.getName(), LIST, LIST);
    } else {
      // return new FixedLengthRecordDefinition(recordType, List.of(fixedFields), List.of(fixedFields))
      definitionBuilder.addStatement("return new $T($S, $T.of(" + fieldList + "))", FIXED_LENGTH_RECORD_DEFINITION, recordDefinition.getName(), LIST);
    }
    recordSpecBuilder.addMethod(definitionBuilder.build());
  }

  private void addFileDefinitionMethod(InterfaceVersion currentVersion, TypeSpec.Builder constantContainerBuilder, List<RecordDefinition> recordDefintions) {
    String recordDefinitionList = recordDefintions.stream()
        .map(RecordDefinition::getName)
        .map(recordName -> recordName + ".definition()")
        .collect(joining(", "));
    Version fileDefinitionVersion = currentVersion.toFileDefinitionVersion();
    
    // public static FileDefinition definition()
    //   return new FileDefinition(FileDefinition.Version.of(major, minor), List.of(recordDefinitions))
    MethodSpec.Builder definitionBuilder = MethodSpec.methodBuilder("definition")
        .returns(FILE_DEFINITION)
        .addModifiers(PUBLIC, STATIC)
        .addStatement("return new $T($T.of($L, $L), $T.of(" + recordDefinitionList + "))",
            FILE_DEFINITION,
            FILE_DEFINITION_VERSION, fileDefinitionVersion.getMajor(), fileDefinitionVersion.getMinor(),
            LIST);
    constantContainerBuilder.addMethod(definitionBuilder.build());
  }
  
  private void addSegmentDefinitionMethod(TypeSpec.Builder recordSpecBuilder, RecordDefinition recordDefinition, int segmentIndicatorIndex, SegmentDefinition segmentDefinition, String segmentIndicator) {
    String fieldList = segmentDefinition.getFields().stream()
        .map(Field::id)
        .collect(joining(", "));
    String segmentIndicatorField = recordDefinition.getName() + "." + segmentIndicator;

    // public static SegmentDefinition definition()
    //   return new SegmentDefinition(segmentIndicatorIndex, segmentIndicatorField, List.of(fields))
    MethodSpec.Builder definitionBuilder = MethodSpec.methodBuilder("definition")
        .returns(SEGMENT_DEFINITION)
        .addModifiers(PUBLIC, STATIC)
        .addStatement("return new $T($L, " + segmentIndicatorField + ", $T.of(" + fieldList + "))", SEGMENT_DEFINITION, segmentIndicatorIndex, LIST);
    recordSpecBuilder.addMethod(definitionBuilder.build());
  }

  private void addSegments(RecordDefinition recordDefinition, ClassName interfaceDefinitionClassName, TypeSpec.Builder recordSpecBuilder) {
    List<String> segmentIndicators = this.segmentIndicatorFieldIdStrategy.getSegmentIndicatorFieldIds(recordDefinition);
    List<String> segmentIndicatorInRecord = new ArrayList<>();
    Iterator<Field> reverseFieldIterator = recordDefinition.getFields().reversed().iterator();
    while (segmentIndicators.size() > segmentIndicatorInRecord.size()) {
      Field field = reverseFieldIterator.next();
      if (segmentIndicators.contains(field.id())) {
        segmentIndicatorInRecord.add(field.id());
      }
    }
    segmentIndicatorInRecord = segmentIndicatorInRecord.reversed();
    
    for (SegmentDefinition segment : recordDefinition.getSegments()) {
      int segmentIndex = segment.getSegmentIndex();
      // public static final class Segment(i + 1)
      TypeSpec.Builder segmentSpecBuilder = TypeSpec.classBuilder(interfaceDefinitionClassName.nestedClass("Segment" + (segmentIndex + 1)))
              .addModifiers(PUBLIC, STATIC, FINAL);
      for (Field field : segment.getFields()) {
        FieldSpec fieldSpec = buildSegmentFieldSpec(segmentIndex, field);
        segmentSpecBuilder.addField(fieldSpec);
      }
      int segmentIndicatorIndex = segmentIndicatorInRecord.indexOf(segmentIndicators.get(segmentIndex));
      String segmentIndicator = segmentIndicators.get(segment.getSegmentIndex());
      addSegmentDefinitionMethod(segmentSpecBuilder, recordDefinition, segmentIndicatorIndex, segment, segmentIndicator);
      recordSpecBuilder.addType(segmentSpecBuilder.build());
    }
  }

  @FunctionalInterface
  interface SegmentIndicatorFieldIdStrategy {

    /**
     * Return the name of the segment indicators in the order as they segments appear. This does
     * not have the be the order as the indicators appear in the record.
     * 
     * @param recordDefinition the record definition
     * @return the segment indicator names ordered in segment order
     */
    List<String> getSegmentIndicatorFieldIds(RecordDefinition recordDefinition);

    static SegmentIndicatorFieldIdStrategy trailingFieldIds() {
      return recordDefinition -> getTrailingFieldsId(recordDefinition, recordDefinition.getSegments().size());
    }

    private static List<String> getTrailingFieldsId(RecordDefinition recordDefinition, int count) {
      List<String> segmentIndicators = new ArrayList<>(count);
      for (Field field : recordDefinition.getFields().reversed()) {
        segmentIndicators.add(field.id());
        if (segmentIndicators.size() == count) {
          break;
        }
      }
      return segmentIndicators.reversed();
    }

  }


  private static FieldSpec buildFieldSpec(Field field) {
    String fieldId = field.id();
    ClassName fieldType = getClassName(field.dataType());
    return FieldSpec.builder(fieldType, fieldId, PUBLIC, STATIC, FINAL)
        .addJavadoc("<h2>$L</h2>", field.name())
        .addJavadoc("\n<p><pre>\n")
        .addJavadoc(field.description().replace("$", "$$")) // escape $
        .addJavadoc("\n</pre></p>")
        .initializer("new $T($S, $L, $L)", fieldType, fieldId, field.length(), field.offset())
        .build();
  }
  
  private static FieldSpec buildSegmentFieldSpec(int segmentIndex, Field field) {
    String fieldId = field.id();
    ClassName delegateType = getClassName(field.dataType());
    TypeName fieldType = ParameterizedTypeName.get(SEGMENT_FIELD_DEFINITION, delegateType);
    return FieldSpec.builder(fieldType, fieldId, PUBLIC, STATIC, FINAL)
        .addJavadoc("<h2>$L</h2>", field.name())
        .addJavadoc("\n<p><pre>\n")
        .addJavadoc(field.description().replace("$", "$$")) // escape $
        .addJavadoc("\n</pre></p>")
        .initializer("new $T($L, new $T($S, $L, $L))", fieldType, segmentIndex, delegateType, fieldId, field.length(), field.offset())
        .build();
  }

  private static ClassName getClassName(DataType dataType) {
    return switch (dataType) {
      case CHAR -> STRING_FIELD_DEFINITION;
      case NUM -> UNSIGNED_FIELD_DEFINITION;
      case SNUM -> SIGNED_FIELD_DEFINITION;
    };
  }

}
