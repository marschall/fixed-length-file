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

  List<RecordDefinition> getRecordDefinitions() {
    return this.recordDefinitions;
  }

  public RecordDefinition getRecordDefinition(String recordType) {
    return this.recordDefinitionMap.get(recordType);
  }
  
  public RecordDefinition getRecordDefinitionFromPrefix(String prefix) {
    // first try direct lookup
    RecordDefinition recordDefinition = this.recordDefinitionMap.get(prefix);
    if (recordDefinition != null) {
      return recordDefinition;
    }
    // fallback to scan
    // actual prefix is shorter than maximum prefix length
    for (Map.Entry<String, RecordDefinition> entry : this.recordDefinitionMap.entrySet()) {
      String recordPrefix = entry.getKey();
      if (prefix.startsWith(recordPrefix)) {
        // TODO put?
        return entry.getValue();
      }
    }
    throw new FileFormatException("unknown record type " + prefix);
  }

  int getMaximumLength() {
    int maxLength = 0;
    for (RecordDefinition recordDefinition : this.recordDefinitions) {
      maxLength = Math.max(maxLength, recordDefinition.getMaximumLength());
    }
    return maxLength;
  }

  int getMaximumPrefixLength() {
    int maximum = 0;
    for (String prefix : this.recordDefinitionMap.keySet()) {
      maximum = Math.max(maximum, prefix.length());
    }
    return maximum;
  }

}
