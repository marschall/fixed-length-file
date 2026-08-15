package com.github.marschall.fixedlenghtfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FileDefinition {

  private final List<RecordDefinition> recordDefinitions;
  private final Map<String, RecordDefinition> recordDefinitionMap;

  public FileDefinition(List<RecordDefinition> recordDefinitions) {
    Objects.requireNonNull(recordDefinitions, "recordDefinitions");
    this.recordDefinitions = recordDefinitions;
    this.recordDefinitionMap = buildRecordDefinitionMap(recordDefinitions);
  }

  private static Map<String, RecordDefinition> buildRecordDefinitionMap(List<RecordDefinition> recordDefinitions) {
    Map<String, RecordDefinition> map = HashMap.newHashMap(recordDefinitions.size());
    for (RecordDefinition recordDefinition : recordDefinitions) {
      map.put(recordDefinition.getType(), recordDefinition);
    }
    return map;
  }

  Map<String, RecordDefinition> getRecordDefinitionMap() {
    return this.recordDefinitionMap;
  }

  public RecordDefinition getRecordDefinition(String recordType) {
    return this.recordDefinitionMap.get(recordType);
  }

  int getMaximumPrefixLength() {
    int maximum = 0;
    for (String prefix : this.recordDefinitionMap.keySet()) {
      maximum = Math.max(maximum, prefix.length());
    }
    return maximum;
  }

}
