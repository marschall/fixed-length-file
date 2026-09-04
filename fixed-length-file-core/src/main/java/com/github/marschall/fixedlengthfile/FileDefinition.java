package com.github.marschall.fixedlengthfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FileDefinition {

  private final List<RecordDefinition> recordDefinitions;
  private final Map<String, RecordDefinition> recordDefinitionMap;
  private final Version version;

  public FileDefinition(Version version, List<RecordDefinition> recordDefinitions) {
    this.version = Objects.requireNonNull(version, "version");
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

  public Version getVersion() {
    return this.version;
  }

  public List<RecordDefinition> getRecordDefinitions() {
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

  public static final class Version {

    private final byte major;
    private final byte minor;

    private Version(byte major, byte minor) {
      this.major = major;
      this.minor = minor;
    }

    public static Version of(int major, int minor) {
      if (major < 0 || major > Byte.MAX_VALUE) {
        throw new IllegalArgumentException();
      }
      if (minor < 0 || minor > Byte.MAX_VALUE) {
        throw new IllegalArgumentException();
      }
      return new Version((byte) major, (byte) minor);
    }

    public int getMajor() {
      return this.major;
    }

    public byte getMinor() {
      return this.minor;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Version)) {
        return false;
      }
      Version other = (Version) obj;
      return (this.major == other.major)
          && (this.minor == other.minor);
    }

    @Override
    public int hashCode() {
      return ((31 + this.major) * 31) + this.minor;
    }

  }

}
