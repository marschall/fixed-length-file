package com.github.marschall.fixedlenghtfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
  
  public static FileDefinitionBuilder builder() {
    return new InternalFileDefinitionBuilder();
  }
  
  static final class InternalFileDefinitionBuilder implements FileDefinitionBuilder {

    @Override
    public void defineRecordType(String recordType,
            Consumer<FixedPartFieldBinder> fixedPartBinder) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public FileDefinition build() {
      // TODO Auto-generated method stub
      return null;
    }
    
  }

}
