package com.github.marschall.fixedlenghtfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileDefinition {

  private final List<RecordDefinition> recordDefinitions;

  public FileDefinition(List<RecordDefinition> recordDefinitions) {
    this.recordDefinitions = recordDefinitions;
  }

  public Map<String, RecordDefinition> getRecordDefinitionMap() {
    Map<String, RecordDefinition> map = HashMap.newHashMap(this.recordDefinitions.size());
    for (RecordDefinition recordDefinition : this.recordDefinitions) {
      map.put(recordDefinition.getPrefix(), recordDefinition);
    }
    return map;
  }

}
