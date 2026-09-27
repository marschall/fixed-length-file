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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.marschall.fixedlengthfile.FileDefinition;
import com.github.marschall.fixedlengthfile.Latin1MemorySegmentWritingLine;
import com.github.marschall.fixedlengthfile.Latin1MemorySegmentWritingLineCreator;
import com.github.marschall.fixedlengthfile.RecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;
import com.github.marschall.fixedlengthfile.SegmentIndicator;
import com.github.marschall.fixedlengthfile.WritingLine;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition267.HD;
import com.github.marschall.fixedlengthfile.configuration.parser.InterfaceDefinition267.KT;
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
    private final Latin1MemorySegmentWritingLineCreator lineCreator;

    State(Latin1MemorySegmentWritingLineCreator lineCreator) {
      this.lineCreator = lineCreator;
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

    Latin1MemorySegmentWritingLineCreator getLineCreator() {
      return this.lineCreator;
    }

  }

  private void writeLines(int lineCount, MemorySegment segment) {
    List<RecordDefinition> recordDefinitions = this.fileDefinition.getRecordDefinitions();

    var state = new State(new Latin1MemorySegmentWritingLineCreator(segment));
    RecordDefinition header = recordDefinitions.getFirst();
    writeHeader(segment, state, this.fileDefinition, (FixedLengthRecordDefinition) header);

    RecordDefinition record = recordDefinitions.get(1);
    writeRecords(segment, state, (SegmentedRecordDefinition) record, lineCount);

    RecordDefinition trailer = recordDefinitions.getLast();
    writeTrailer(segment, state, (FixedLengthRecordDefinition) trailer);
  }

  private void writeRecords(MemorySegment segment, State state, SegmentedRecordDefinition definition, int count) {
    List<SegmentDefinition> segmentDefinitions = definition.getSegmentDefinitions();
    List<SegmentIndicator> segmentIndicators = Collections.nCopies(segmentDefinitions.size(), SegmentIndicator.ABSENT);
    for (int i = 0; i < count; i++) {
      this.writeRecord(segment, state, definition, segmentIndicators);
    }
  }

  private void writeRecord(MemorySegment segment, State state, SegmentedRecordDefinition definition,  List<SegmentIndicator> segmentIndicators) {
    int lineLength = writeEmptySegmentedLine(segment, state, definition, segmentIndicators, line -> {
      line.writeString(KT.KT01, "KT");
      line.writeUnsignedInt(KT.KT02, state.getCurrentRecordSequenceNumber());
    });
    this.crLf(segment, state.getLineStart() + lineLength);
    state.addLine(lineLength + 2);
  }

  private void writeHeader(MemorySegment segment, State state, FileDefinition fieldDefintion, FixedLengthRecordDefinition recordDefinition) {
    int lineLength = writeEmptyFixedLine(segment, state, recordDefinition, line -> {
      line.writeString(HD.H01, "HD");
      line.writeUnsignedInt(HD.H02, state.getCurrentRecordSequenceNumber());
      line.writeString(HD.H12, toInterfaceVersion(fieldDefintion.getVersion()));
    });
    this.crLf(segment, state.getLineStart() + lineLength);
    state.addLine(lineLength + 2);
  }

  private void writeTrailer(MemorySegment segment, State state, FixedLengthRecordDefinition recordDefinition) {
    int lineLength = writeEmptyFixedLine(segment, state, recordDefinition, line -> {
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

  private int writeEmptySegmentedLine(MemorySegment fileSegment, State state, SegmentedRecordDefinition segmentedRecordDefinition, List<SegmentIndicator> segmentIndicators, Consumer<WritingLine> lineConsumer) {
    int recordLength = segmentedRecordDefinition.getBaseLength();
    Latin1MemorySegmentWritingLine line = state.getLineCreator().writingLineFor(segmentedRecordDefinition, segmentIndicators);

    lineConsumer.accept(line);
    return recordLength;
  }

  private int writeEmptyFixedLine(MemorySegment fileSegment, State state, FixedLengthRecordDefinition fixedRecordDefinition, Consumer<WritingLine> lineConsumer) {
    int recordLength = fixedRecordDefinition.getBaseLength();

    Latin1MemorySegmentWritingLine line = state.getLineCreator().writingLineFor(fixedRecordDefinition);
    lineConsumer.accept(line);

    return recordLength;
  }

  static String toInterfaceVersion(FileDefinition.Version version) {
    return "%1$d%2$02d".formatted(version.getMajor(), version.getMinor());
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
