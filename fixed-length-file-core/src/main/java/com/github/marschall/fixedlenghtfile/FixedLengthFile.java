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
    this.maximumPrefixLength = fileDefinition.getMaximumPrefixLength();
    this.recordDefinitionMap = fileDefinition.getRecordDefinitionMap();
    this.segment = segment;
  }

  public void parseFile(LineConsumer consumer) {
    if (this.segment.byteSize() == 0) {
      return;
    }
    long position = 0;
    int recordNumber = 0;
    while (position < this.segment.byteSize()) {
      RecordDefinition recordDefinition = determineRecordDefinition(position);
      int recordLength = determineRecordLength(position, recordDefinition);
      MemorySegment lineSegment = this.segment.asSlice(position, recordLength);
      Latin1MemorySegmentReadingLine line = new Latin1MemorySegmentReadingLine(lineSegment);
      consumer.accept(recordDefinition.getType(), recordNumber, line);
      recordNumber += 1;
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
    byte[] characters = this.segment.asSlice(position, this.maximumPrefixLength).toArray(JAVA_BYTE);
    String prefix = new String(characters, ISO_8859_1);
    // first try direct lookup
    RecordDefinition recordDefinition = this.fileDefinition.getRecordDefinition(prefix);
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
