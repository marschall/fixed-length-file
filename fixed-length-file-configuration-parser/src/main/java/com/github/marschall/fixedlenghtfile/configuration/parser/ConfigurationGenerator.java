package com.github.marschall.fixedlenghtfile.configuration.parser;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.github.marschall.fixedlenghtfile.configuration.parser.RecordDefinitionFragment.RecordDefinition;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;

public class ConfigurationGenerator {
  
  public void generateTo(InterfaceVersion currentVersion, Set<String> interestingRecordTypes, Path interfacePath, Path outputDirectory, String packageName)
      throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
    ConfigurationParser parser = new ConfigurationParser(currentVersion);
    List<RecordDefinition> recordDefinitions = parser.parse(interfacePath, interestingRecordTypes);
    generate(recordDefinitions, outputDirectory, packageName, "InterfaceDefinition");
  }
  
  private void generate(List<RecordDefinition> recordDefintions, Path outputDirectory, String packageName, String className) throws IOException {
    TypeSpec constantContainer = TypeSpec.classBuilder(ClassName.get(packageName, className))
        .addModifiers(PUBLIC, FINAL)
        .build();

    JavaFile javaFile = JavaFile.builder(packageName, constantContainer)
        .build();

    javaFile.writeToPath(outputDirectory);
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}
