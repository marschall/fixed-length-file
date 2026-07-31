package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.lang.foreign.MemorySegment;
import java.util.Map;

public final class FixedLengthFile {
  
  private final byte CR = 13;
  private final byte LF = 10;

  private final FileDefinition fileDefinition;
  private final MemorySegment segment;
  private final Map<String, RecordDefinition> recordDefinitionMap;
  private final int maximumPrefixLength;

  FixedLengthFile(FileDefinition fileDefinition, MemorySegment segment) {
    this.fileDefinition = fileDefinition;
    this.recordDefinitionMap = this.fileDefinition.getRecordDefinitionMap();
    this.maximumPrefixLength = this.getMaximumPrefixLength();
    this.segment = segment;
  }

  private int getMaximumPrefixLength() {
    int maximum = 0;
    for (String prefix : this.recordDefinitionMap.keySet()) {
      maximum = Math.max(maximum, prefix.length());
    }
    return maximum;
  }

  void parseFile() {
    if (this.segment.byteSize() == 0) {
      return;
    }
    long position = 0;
    while (position < this.segment.byteSize()) {
      RecordDefinition recordDefinition = determineRecordDefinition(position);
      int recordLength = determineRecordLength(position, recordDefinition);
      MemorySegment lineSegment = this.segment.asSlice(position, recordLength);
      Latin1MemorySegmentReadingLine line = new Latin1MemorySegmentReadingLine(lineSegment, 0L);
      position = this.advanceBeyondNewline(position + recordLength);
    }
  }

  private long advanceBeyondNewline(long position) {
    if (this.segment.byteSize() == position) {
      return position;
    }
    byte b1 = this.segment.getAtIndex(JAVA_BYTE, position);
    if (b1 == LF) {
      return position + 1;
    }
    if (b1 == CR) {
      if (this.segment.byteSize() == position + 1) {
        return position;
      }
      byte b2 = this.segment.getAtIndex(JAVA_BYTE, position + 1);
      return b2 == LF ? position + 1 : position;
    }
    throw new FileFormatException("expected newline at: " + position);
  }

  private RecordDefinition determineRecordDefinition(long position) {
    if (position + this.maximumPrefixLength >= this.segment.byteSize()) {
      throw new FileFormatException("expected a minium of " + this.maximumPrefixLength + " to determine record type");
    }
    String prefix = this.segment.asSlice(position, this.maximumPrefixLength).getString(0L, ISO_8859_1);
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
        return entry.getValue();
      }
    }
    throw new FileFormatException("unknown record type " + prefix);
  }

  private int determineRecordLength(long position, RecordDefinition recordDefinition) {
    return recordDefinition.determineRecordLengt(this.segment, position);
  }

}
