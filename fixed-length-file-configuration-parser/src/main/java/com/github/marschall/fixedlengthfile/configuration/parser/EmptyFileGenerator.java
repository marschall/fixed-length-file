package com.github.marschall.fixedlengthfile.configuration.parser;

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
import java.util.function.Consumer;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.SignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.FileDefinition;
import com.github.marschall.fixedlengthfile.Latin1MemorySegmentWritingLine;
import com.github.marschall.fixedlengthfile.RecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;
import com.github.marschall.fixedlengthfile.SegmentIndicator;
import com.github.marschall.fixedlengthfile.WritingLine;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition267.KT;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition267.HD;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition267.TR;

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

  static final class State {

    private int currentRecordSequenceNumber;
    private int lineStart;

    State() {
      this.currentRecordSequenceNumber = 0;
      this.lineStart = 0;
    }

    int getLineStart() {
      return this.lineStart;
    }

    int getCurrentRecordSequenceNumber() {
      return this.currentRecordSequenceNumber;
    }

    void addLine(int length) {
      this.lineStart += length;
      this.currentRecordSequenceNumber += 1;
    }

  }

  private void writeLines(int lineCount, MemorySegment segment) {
    List<RecordDefinition> recordDefinitions = this.fileDefinition.getRecordDefinitions();

    var state = new State();
    RecordDefinition header = recordDefinitions.getFirst();
    writeHeader(segment, state, (FixedLengthRecordDefinition) header);

    RecordDefinition record = recordDefinitions.get(1);
    writeRecords(segment, state, (SegmentedRecordDefinition) record, lineCount);

    RecordDefinition trailer = recordDefinitions.getLast();
    writeTrailer(segment, state, (FixedLengthRecordDefinition) trailer);
  }
  
  private void writeRecords(MemorySegment segment, State state, SegmentedRecordDefinition definition, int count) {

    List<SegmentDefinition> segmentDefinitions = definition.getSegmentDefinitions();
    Set<StringFieldDefinition> segmentIndicatorFields = HashSet.newHashSet(segmentDefinitions.size());
    for (SegmentDefinition segmentDefinition : segmentDefinitions) {
      segmentIndicatorFields.add(segmentDefinition.getSegmentIndicatorField());
    }
    for (int i = 0; i < count; i++) {
      this.writeRecord(segment, state, definition, segmentIndicatorFields);
    }
    
  }

  private void writeRecord(MemorySegment segment, State state, SegmentedRecordDefinition definition, Set<StringFieldDefinition> segmentIndicatorFields) {
    int lineLength = writeEmptySegmentedLine(segment, state, definition, segmentIndicatorFields, line -> {
      line.writeString(KT.KT01, "KT");
      line.writeUnsignedInt(KT.KT02, state.getCurrentRecordSequenceNumber());
    });
    this.crLf(segment, state.getLineStart() + lineLength);
    state.addLine(lineLength + 2);
  }

  private void writeHeader(MemorySegment segment, State state, FixedLengthRecordDefinition definition) {
    int lineLength = writeEmptyFixedLine(segment, state, definition, line -> {
      line.writeString(HD.H01, "HD");
      line.writeUnsignedInt(HD.H02, state.getCurrentRecordSequenceNumber());
      // TODO version
    });
    this.crLf(segment, state.getLineStart() + lineLength);
    state.addLine(lineLength + 2);
  }
  
  private void writeTrailer(MemorySegment segment, State state, FixedLengthRecordDefinition definition) {
    int lineLength = writeEmptyFixedLine(segment, state, definition, line -> {
      line.writeString(TR.T01, "TR");
      line.writeUnsignedInt(TR.T02, state.getCurrentRecordSequenceNumber());
    });
    this.crLf(segment, state.getLineStart() + lineLength);
    state.addLine(lineLength + 2);
  }

  private void crLf(MemorySegment segment, long position) {
    segment.set(ValueLayout.JAVA_BYTE, position, CR);
    segment.set(ValueLayout.JAVA_BYTE, position + 1, LF);
  }

  private int writeEmptySegmentedLine(MemorySegment fileSegment, State state, SegmentedRecordDefinition segmentedRecordDefinition, Set<StringFieldDefinition> segmentIndicatorFields, Consumer<WritingLine> lineConsumer) {
    int recordLength = segmentedRecordDefinition.getBaseLength();
    MemorySegment recordSegment = fileSegment.asSlice(state.getLineStart(), recordLength);
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
    lineConsumer.accept(line);
    return recordLength;
  }

  private int writeEmptyFixedLine(MemorySegment fileSegment, State state, FixedLengthRecordDefinition fixedRecordDefinition, Consumer<WritingLine> lineConsumer) {
    int recordLength = fixedRecordDefinition.getBaseLength();
    MemorySegment recordSegment = fileSegment.asSlice(state.getLineStart(), recordLength);
    
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
    lineConsumer.accept(line);
    
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
