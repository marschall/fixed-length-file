package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Map;

import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.FieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;

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
  
//  public int countLines(String recordType) {
//    // TODO
//    // TODO map record count of type to global record count
//    // TODO safe offset 
//    return 0;
//  }
//  
//  public <T> T parseLine(RecordIdentifier identifier, LineMapper<T> mapper) {
//    return mapper.map(null, null, null);
//  }
//  
//  final class RecordIdentifier {
//    
//  }

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
      Latin1MemorySegmentReadingLine line = switch (recordDefinition) {
        case FixedLengthRecordDefinition _ ->  {
          yield new FixedLatin1MemorySegmentReadingLine(lineSegment);
        }
        case SegmentedRecordDefinition segmented -> {
          // TODO read only once
          SegmentOffsets segmentOffsets = readSegmentOffsets(position, segmented);
          yield new SegmentedLatin1MemorySegmentReadingLine(lineSegment, segmentOffsets);
        }
      };
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
  
  private SegmentOffsets readSegmentOffsets(long lineStart, SegmentedRecordDefinition recordDefinition) {
    List<SegmentDefinition> segmentDefinitions = recordDefinition.getSegmentDefinitions();
    int offset = recordDefinition.getBaseLength();
    var segmentOffsets = new SegmentOffsets(segmentDefinitions.size());
    for (int i = 0; i < segmentDefinitions.size(); i++) {
      var segmentDefinition = segmentDefinitions.get(i);
      StringFieldDefinition segmentIndicatorField = segmentDefinition.getSegmentIndicatorField();
      var segmentIndicator = readSegmentIndicator(lineStart, recordDefinition, segmentIndicatorField);
      switch (segmentIndicator) {
      case PRESENT -> {
        segmentOffsets.setSegmentOffset(i, offset);
        offset += segmentDefinition.getLength();
      }
      case SPACES -> {
        segmentOffsets.setSegmentIsSpaces(i);
        offset += segmentDefinition.getLength();
      }
      case ABSENT -> {
        segmentOffsets.setSegmentNotPresent(i);
      }
      };

    }
    return segmentOffsets;
  }
  
  private SegmentIndicator readSegmentIndicator(long lineStart, SegmentedRecordDefinition recordDefinition, StringFieldDefinition segmentIndicatorFieldDefinition) {
    // TODO bind and cache
    BoundSegmentIndicatorField segmentIndicatorField = recordDefinition.bindSegmentIndicatorField(segmentIndicatorFieldDefinition);
    byte b = this.segment.getAtIndex(JAVA_BYTE, lineStart + segmentIndicatorField.getOffset());
    char c = (char) Byte.toUnsignedInt(b);
    return switch (c) {
      case SegmentIndicator.PRESENT_VALUE -> SegmentIndicator.PRESENT;
      case SegmentIndicator.SPACES_VALUE -> SegmentIndicator.SPACES;
      case SegmentIndicator.ABSENT_VALUE -> SegmentIndicator.ABSENT;
      default -> throw new FileFormatException("Unexpected segment indicator: " + c);
    };
  }
  
  private int computeRecordLength(long lineStart, SegmentedRecordDefinition recordDefinition) {
    int length = recordDefinition.getBaseLength();
    var segmentOffsets = readSegmentOffsets(lineStart, recordDefinition);
    List<SegmentDefinition> segmentDefinitions = recordDefinition.getSegmentDefinitions();
    for (int i = 0; i < segmentDefinitions.size(); i++) {
      var segmentDefinition = segmentDefinitions.get(i);
      int segmentOffset = segmentOffsets.getSegmentOffset(i);
      if (segmentOffset != SegmentOffsets.SEGMENT_NOT_PRESENT) {
        length += segmentDefinition.getLength();
      }
    }
    return length;
  }
  

  private int determineRecordLength(long position, RecordDefinition recordDefinition) {
    return switch (recordDefinition) {
      case FixedLengthRecordDefinition fixed -> fixed.getMaximumLength();
      case SegmentedRecordDefinition segmented -> computeRecordLength(position, segmented);
    };
  }

}
