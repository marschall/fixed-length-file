package com.github.marschall.fixedlengthfile;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.github.marschall.fixedlengthfile.FileInformation.Unknown;
import com.github.marschall.fixedlengthfile.FileInformation.Versioned.Main;
import com.github.marschall.fixedlengthfile.FileInformation.Versioned.Variant;

public final class FileDefinitionRepository {

  private Map<FileDefinition.Version, FileDefinition> mainFileDefinitions;
  private Map<FileDefinition.Version, FileDefinition> variantFileDefinitions;

  private FileDefinitionRepository(List<FileDefinition> mainFileDefinitions, List<FileDefinition> variantFileDefinitions) {
    this.mainFileDefinitions = buildVersionMap(mainFileDefinitions);
    this.variantFileDefinitions = buildVersionMap(variantFileDefinitions);
  }
  
  private static Map<FileDefinition.Version, FileDefinition> buildVersionMap(List<FileDefinition> fileDefinitions) {
    return fileDefinitions.stream()
        .collect(toMap(FileDefinition::getVersion, Function.identity()));
  }
  
  public FileDefinition getFileDefinition(FileInformation fileInformation) {
    FileDefinition fileDefinition = switch (fileInformation) {
      case Main main -> this.mainFileDefinitions.get(main.getVersion());
      case Variant variant -> this.variantFileDefinitions.get(variant.getVersion());
      case Unknown _ -> throw new IllegalArgumentException("Unknonw file format");
    };
    if (fileDefinition == null) {
      throw new IllegalArgumentException("Unknonw file format: " + fileInformation);
    }
    return fileDefinition;
  }

  public static FileDefinitionRepositoryBuilder builder() {
    return new FileDefinitionRepositoryBuilder();
  }

  public static final class FileDefinitionRepositoryBuilder {

    private final List<FileDefinition> mainFileDefinitions;
    private final List<FileDefinition> variantFileDefinitions;

    private FileDefinitionRepositoryBuilder() {
      this.mainFileDefinitions = new ArrayList<>();
      this.variantFileDefinitions = new ArrayList<>();
    }

    public FileDefinitionRepositoryBuilder addMainFileDefinition(FileDefinition fileDefinition) {
      this.mainFileDefinitions.add(Objects.requireNonNull(fileDefinition));
      return this;
    }

    public FileDefinitionRepositoryBuilder addVariantFileDefinition(FileDefinition fileDefinition) {
      this.variantFileDefinitions.add(Objects.requireNonNull(fileDefinition));
      return this;
    }

    public FileDefinitionRepository build() {
      return new FileDefinitionRepository(this.mainFileDefinitions, this.variantFileDefinitions);
    }

  }

}
