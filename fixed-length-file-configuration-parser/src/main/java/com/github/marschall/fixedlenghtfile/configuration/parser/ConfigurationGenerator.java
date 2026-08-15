package com.github.marschall.fixedlenghtfile.configuration.parser;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.github.marschall.fixedlenghtfile.configuration.parser.RecordDefinitionFragment.RecordDefinition;
import com.github.marschall.fixedlenghtfile.configuration.parser.RecordDefinitionFragment.SegmentDefinition;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeName;

public class ConfigurationGenerator {
  
  private static final ClassName SIGNED_FIELD_DEFINITION = ClassName.get("com.github.marschall.fixedlenghtfile", "FieldDefinition", "OffsetFieldDefinition", "SignedFieldDefinition");
  private static final ClassName UNSIGNED_FIELD_DEFINITION = ClassName.get("com.github.marschall.fixedlenghtfile", "FieldDefinition", "OffsetFieldDefinition", "UnsignedFieldDefinition");
  private static final ClassName STRING_FIELD_DEFINITION = ClassName.get("com.github.marschall.fixedlenghtfile", "FieldDefinition", "OffsetFieldDefinition", "StringFieldDefinition");
  private static final ClassName SEGMENT_FIELD_DEFINITION = ClassName.get("com.github.marschall.fixedlenghtfile", "FieldDefinition", "SegmentFieldDefinition");

  public void generateTo(InterfaceVersion currentVersion, Set<String> interestingRecordTypes, Path interfacePath, Path outputDirectory, String packageName)
      throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    ConfigurationParser parser = new ConfigurationParser(currentVersion);
    List<RecordDefinition> recordDefinitions = parser.parse(interfacePath, interestingRecordTypes);
    String className = "InterfaceDefinition" + currentVersion.toInterfaceString();
    generate(recordDefinitions, outputDirectory, packageName, className);
  }
  
  private void generate(List<RecordDefinition> recordDefintions, Path outputDirectory, String packageName, String className) throws IOException {
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
      MethodSpec.Builder definitionBuilder = MethodSpec.methodBuilder("definition")
          .returns(ClassName.get("com.github.marschall.fixedlenghtfile", "RecordDefinition"))
          .addModifiers(PUBLIC, STATIC)
          .addStatement("return null");
      recordSpecBuilder.addMethod(definitionBuilder.build());
      if (recordDefinition.hasSegments()) {
        int segmentIndex = 1;
        for (SegmentDefinition segment : recordDefinition.getSegments()) {
          TypeSpec.Builder segmentSpecBuilder = TypeSpec.classBuilder(interfaceDefinitionClassName.nestedClass("Segment" + (segmentIndex + 1)))
                  .addModifiers(PUBLIC, STATIC, FINAL);
          for (Field field : segment.getFields()) {
            FieldSpec fieldSpec = buildSegmentFieldSpec(segmentIndex, field);
            segmentSpecBuilder.addField(fieldSpec);
          }
          recordSpecBuilder.addType(segmentSpecBuilder.build());
          segmentIndex += 1;
        }
      }
      constantContainerBuilder.addType(recordSpecBuilder.build());
    }

    JavaFile javaFile = JavaFile.builder(packageName, constantContainerBuilder.build())
        .build();

    javaFile.writeToPath(outputDirectory);
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

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
