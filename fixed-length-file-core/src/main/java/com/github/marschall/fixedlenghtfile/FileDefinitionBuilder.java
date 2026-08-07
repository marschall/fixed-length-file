package com.github.marschall.fixedlenghtfile;

import java.util.function.Consumer;

public interface FileDefinitionBuilder {

  FileDefinitionBuilder defineRecordType(String recordType, Consumer<FixedPartFieldBinder> binderConsumer);

  FileDefinition build();

}
