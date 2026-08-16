package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Map;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;

public abstract sealed class FixedLengthFile
  permits StatefulFixedLengthFile, StatelessFixedLengthFile {

  private final byte CR = 13;
  private final byte LF = 10;

  private final FileDefinition fileDefinition;
  protected final MemorySegment segment;
  private final Map<String, RecordDefinition> recordDefinitionMap;
  private final int maximumPrefixLength;

  protected FixedLengthFile(FileDefinition fileDefinition, MemorySegment segment) {
    this.fileDefinition = fileDefinition;
    this.maximumPrefixLength = fileDefinition.getMaximumPrefixLength();
    this.recordDefinitionMap = fileDefinition.getRecordDefinitionMap();
    this.segment = segment;
  }

  protected LineInformation preParseLine(long lineStart) {
    RecordDefinition recordDefinition = determineRecordDefinition(lineStart);
    return switch (recordDefinition) {
      case FixedLengthRecordDefinition fixed -> {
        yield new LineInformation(fixed, determineRecordLength(lineStart, fixed), null);
      }
      case SegmentedRecordDefinition segmented -> {
        SegmentOffsets segmentOffsets = readSegmentOffsets(lineStart, segmented);
        yield new LineInformation(segmented, determineRecordLength(lineStart, segmented, segmentOffsets), segmentOffsets);
      }
    };
  }

  record LineInformation(RecordDefinition recordDefinition, int recordLength, SegmentOffsets segmentOffsets) {

  }

  
  protected ReadingLine asLine(long lineStart, LineInformation lineInformation, RecordDefinition recordDefinition) {
    int recordLength = lineInformation.recordLength();
    MemorySegment lineSegment = this.segment.asSlice(lineStart, recordLength);
    return switch (recordDefinition) {
      case FixedLengthRecordDefinition _ ->  {
        yield new FixedLatin1MemorySegmentReadingLine(lineSegment);
      }
      case SegmentedRecordDefinition _ -> {
        yield new SegmentedLatin1MemorySegmentReadingLine(lineSegment, lineInformation.segmentOffsets());
      }
    };
  }

  protected long advanceBeyondNewline(long position) {
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

  protected RecordDefinition determineRecordDefinition(long lineStart) {
    if (lineStart + this.maximumPrefixLength >= this.segment.byteSize()) {
      throw new FileFormatException("expected a minium of " + this.maximumPrefixLength + " to determine record type");
    }
    byte[] characters = this.segment.asSlice(lineStart, this.maximumPrefixLength).toArray(JAVA_BYTE);
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
        // TODO put?
        return entry.getValue();
      }
    }
    throw new FileFormatException("unknown record type " + prefix);
  }

  protected SegmentOffsets readSegmentOffsets(long lineStart, SegmentedRecordDefinition recordDefinition) {
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
    byte b = this.segment.getAtIndex(JAVA_BYTE, lineStart + segmentIndicatorFieldDefinition.getOffset());
    char c = (char) Byte.toUnsignedInt(b);
    return switch (c) {
    case SegmentIndicator.PRESENT_VALUE -> SegmentIndicator.PRESENT;
    case SegmentIndicator.SPACES_VALUE -> SegmentIndicator.SPACES;
    case SegmentIndicator.ABSENT_VALUE -> SegmentIndicator.ABSENT;
    default -> throw new FileFormatException("Unexpected segment indicator: " + c);
    };
  }

  private int computeRecordLength(long lineStart, SegmentedRecordDefinition recordDefinition, SegmentOffsets segmentOffsets) {
    int length = recordDefinition.getBaseLength();
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
  
  protected int determineRecordLength(long lineStart, FixedLengthRecordDefinition recordDefinition) {
    return recordDefinition.getMaximumLength();
  }
  
  protected int determineRecordLength(long lineStart, SegmentedRecordDefinition recordDefinition, SegmentOffsets segmentOffsets) {
    return computeRecordLength(lineStart, recordDefinition, segmentOffsets);
  }


}
