package com.github.marschall.fixedlenghtfile;

import java.util.function.Consumer;

public interface FileDefinitionBuilder {

  void defineRecordType(String recordType, Consumer<FixedPartFieldBinder> fixedPartBinder);

  FileDefinition build();

}
