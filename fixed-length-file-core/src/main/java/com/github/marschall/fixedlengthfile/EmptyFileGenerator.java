package com.github.marschall.fixedlengthfile;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.SignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;

public final class EmptyFileGenerator {

  private final byte CR = 13;
  private final byte LF = 10;

  private final FileDefinition fileDefinition;

  public EmptyFileGenerator(FileDefinition fileDefinition) {
    this.fileDefinition = Objects.requireNonNull(fileDefinition, "fileDefinition");
  }

  public void generateFile(Path output, int lineCount) throws IOException {
    long fileSize = this.computeTotalFileSize(lineCount);
    try (FileChannel channel = FileChannel.open(output, CREATE_NEW, READ, WRITE)) {
//      channel.truncate(fileSize);
      FileLock fileLock = channel.lock(0, fileSize, false);
      try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = channel.map(READ_WRITE, 0, fileSize, arena);
        writeLines(lineCount, segment);
      } finally {
        fileLock.release();
      }
    }
  }

  private void writeLines(int lineCount, MemorySegment segment) {
    List<RecordDefinition> recordDefinitions = this.fileDefinition.getRecordDefinitions();
    long lineStart = 0L;

    RecordDefinition header = recordDefinitions.getFirst();
    lineStart = this.writeEmptyLine(segment, lineStart, header);

    RecordDefinition record = recordDefinitions.get(1);
    // TODO make copy
    for (int i = 0; i < lineCount; i++) {
      lineStart = this.writeEmptyLine(segment, lineStart, record);
    }

    RecordDefinition trailer = recordDefinitions.getLast();
    lineStart = this.writeEmptyLine(segment, lineStart, trailer);
  }

  private long writeEmptyLine(MemorySegment segment, long lineStart, RecordDefinition definition) {
    long lineLength = switch (definition) {
      case FixedLengthRecordDefinition fixed -> writeEmptyFixedLine(segment, lineStart, fixed);
      case SegmentedRecordDefinition segmented -> writeEmptyFixedLine(segment, lineStart, segmented);
    };
    this.crLf(segment, lineStart + lineLength);
    return lineStart + lineLength + 2;
  }

  private void crLf(MemorySegment segment, long position) {
    segment.set(ValueLayout.JAVA_BYTE, position, CR);
    segment.set(ValueLayout.JAVA_BYTE, position + 1, LF);
  }

  private long writeEmptyFixedLine(MemorySegment fileSegment, long lineStart, SegmentedRecordDefinition segmentedRecordDefinition) {
    int recordLength = segmentedRecordDefinition.getBaseLength();
    List<SegmentDefinition> segmentDefinitions = segmentedRecordDefinition.getSegmentDefinitions();
    Set<StringFieldDefinition> segmentIndicatorFields = HashSet.newHashSet(segmentDefinitions.size());
    for (SegmentDefinition segmentDefinition : segmentDefinitions) {
      // TODO optimize
      segmentIndicatorFields.add(segmentDefinition.getSegmentIndicatorField());
    }
    MemorySegment recordSegment = fileSegment.asSlice(lineStart, recordLength);
    WritingLine line = new Latin1MemorySegmentWritingLine(recordSegment);
    List<? extends OffsetFieldDefinition> fields = segmentedRecordDefinition.getFixedFields();
    writeTypeField(segmentedRecordDefinition, fields.getFirst(), line);
    for (OffsetFieldDefinition field : fields.subList(1, fields.size())) {
      switch (field) {
        case StringFieldDefinition stringField -> {
          if (segmentIndicatorFields.contains(stringField)) {
            line.writeSegmentIndicator(stringField, SegmentIndicator.ABSENT);
          } else {
            line.writeNoValue(stringField);
          }
        }
        case UnsignedFieldDefinition unsignedField -> line.writeNoValue(unsignedField);
        case SignedFieldDefinition _ -> throw new UnsupportedOperationException("Unsigned not yet supported");
      };
    }
    return recordLength;
  }

  private long writeEmptyFixedLine(MemorySegment fileSegment, long lineStart, FixedLengthRecordDefinition fixedRecordDefinition) {
    int recordLength = fixedRecordDefinition.getBaseLength();
    MemorySegment recordSegment = fileSegment.asSlice(lineStart, recordLength);
    WritingLine line = new Latin1MemorySegmentWritingLine(recordSegment);
    List<? extends OffsetFieldDefinition> fields = fixedRecordDefinition.getFields();
    writeTypeField(fixedRecordDefinition, fields.getFirst(), line);
    for (OffsetFieldDefinition field : fields.subList(1, fields.size())) {
      switch (field) {
        case StringFieldDefinition stringField -> line.writeNoValue(stringField);
        case UnsignedFieldDefinition unsignedField -> line.writeNoValue(unsignedField);
        case SignedFieldDefinition _ -> throw new UnsupportedOperationException("Unsigned not yet supported");
      };
    }
    return recordLength;
  }
  
  private void writeTypeField(RecordDefinition recordDefinition, OffsetFieldDefinition fieldDefintion, WritingLine line) {
    String recordType = recordDefinition.getType();
    int expectedLength = recordType.length();
    if (fieldDefintion.getLength() != expectedLength) {
      throw new IllegalStateException("expected type field of length: ");
    }
    if (!(fieldDefintion instanceof StringFieldDefinition stringField)) {
      throw new IllegalStateException("expected type field " + fieldDefintion + " to be of type String");
    }
    line.writeString(stringField, recordType);
  }

  private long computeTotalFileSize(int lineCount) {
    List<RecordDefinition> recordDefinitions = fileDefinition.getRecordDefinitions();
    // header
    long totalFileSize = recordDefinitions.getFirst().getBaseLength() + 2;
    // records
    totalFileSize += (recordDefinitions.get(1).getBaseLength() + 2L) * lineCount;
    // trailer
    totalFileSize += recordDefinitions.getLast().getBaseLength() + 2;
    return totalFileSize;
  }

}
