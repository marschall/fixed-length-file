package com.github.marschall.fixedlengthfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.io.IOException;
import java.io.Reader;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;

final class Latin1MemorySegmentReadingLine extends AbstractReadingLine {

  private final MemorySegment memorySegment;

  private final SegmentOffsets segmentOffsets;

  Latin1MemorySegmentReadingLine(MemorySegment segment, SegmentOffsets segmentOffsets) {
    this.memorySegment = segment;
    this.segmentOffsets = Objects.requireNonNull(segmentOffsets, "segmentOffsets");
  }

  @Override
  protected SegmentOffsets getSegmentOffsets() {
    return this.segmentOffsets;
  }

  @Override
  public int readUnsignedInt(UnsignedFieldDefinition field) {
    return readUnsignedInt(0, field);
  }

  protected int readUnsignedInt(int baseOffset, UnsignedFieldDefinition field) {
    int offset = baseOffset + field.getOffset();
    return readUnsignedIntSafe(offset, field.getLength());
  }

  int readUnsignedIntSafe(int offset, int length) {
    int value = 0;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c == ' ') {
        // TODO invalid
        return value;
      }
      if (c < '0' || c > '9') {
        throw digitExpectedAt(offset + i, c);
      }
      value = value * 10 + (c - '0');
    }
    return value;
  }

  int readUnsignedInt(int offset, int length) {
    // TODO implements CharSequence -> Integer.parseInt
    int value = 0;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c < '0' || c > '9') {
        throw digitExpectedAt(offset + i, c);
      }
      value = value * 10 + (c - '0');
    }
    return value;
  }

  private char readCharAt(int index) {
    byte b = this.memorySegment.getAtIndex(JAVA_BYTE, index);
    return (char) Byte.toUnsignedInt(b);
  }

  private RuntimeException digitExpectedAt(int i, char c) {
    return new FileFormatException("expected digit at index: " + (this.memorySegment.address() + i) + " but got: " + c);
  }

  @Override
  public long readUnsignedLong(UnsignedFieldDefinition field) {
    return readUnsignedLong(0, field);
  }

  protected long readUnsignedLong(int baseOffset, UnsignedFieldDefinition field) {
    return readUnsignedLong(baseOffset + field.getOffset(), field.getLength());
  }

  private long readUnsignedLong(int offset, int length) {
    long value = 0L;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c < '0' || c > '9') {
        throw digitExpectedAt(offset + i, c);
      }
      value = value * 10L + (c - '0');
    }
    return value;
  }

  @Override
  public String readTrimmedString(StringFieldDefinition field) {
    return readTrimmedString(0, field);
  }
  
  protected String readTrimmedString(int baseOffset, StringFieldDefinition field) {
    int offset = baseOffset + field.getOffset();
    int length = field.getLength();
    // initialize with end in case string is all spaces
    int start = offset + length;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c != ' ') {
        start = offset + i;
        break;
      }
    }
    if (start == offset + length) {
      // avoid allocation for the common case of an empty string
      return "";
    }
    // search last non space
    int end = offset + length - 1;
    for (int l = end; l >= start; l--) {
      char c = readCharAt(l);
      if (c != ' ') {
        end = l;
        break;
      }
    }
    int bufferLength = (int) (end - start) + 1;
    byte[] buffer = new byte[bufferLength];
    MemorySegment.copy(this.memorySegment, JAVA_BYTE, start, buffer, 0, bufferLength);
    // REVIEW this.segment.asSlice().getString() would avoid one copy
    return new String(buffer, 0, bufferLength, ISO_8859_1);
  }

  String readAllAsString(int start) {
    int bufferLength = size() - start;
    byte[] buffer = new byte[bufferLength];
    MemorySegment.copy(this.memorySegment, JAVA_BYTE, start, buffer, 0, bufferLength);
    // REVIEW this.segment.asSlice().getString() would avoid one copy but require terminating 0
    return new String(buffer, 0, bufferLength, ISO_8859_1);
  }

  int transferFromTo(int start, char[] cbuf, int off, int len) {
    int toRead = Math.min(this.size() - start, len);
    for (int i = 0; i < toRead; i++) {
      cbuf[off + i] = this.readCharAt(start + i);
      
    }
    return toRead;
  }

  int size() {
    return Math.toIntExact(this.memorySegment.byteSize());
  }

  @Override
  public SegmentIndicator readSegmentIndicator(StringFieldDefinition field) {
    int offset = field.getOffset();
    char c = readCharAt(offset);
    return toSegmentIndicator(c);
  }

  @Override
  public int getLength() {
    return size();
  }

  @Override
  public Reader asReader() {
    return new MemorySegmentReader();
  }

  final class MemorySegmentReader extends Reader {
    // no mark because ojdbc does not call it
    // no transferTo ojdbc does not call it

    private boolean closed;

    // TODO short for packing
    private int position;

    MemorySegmentReader() {
      this.position = 0;
      this.closed = false;
    }

    @Override
    public boolean ready() throws IOException {
      return !atEnd();
    }

    private boolean atEnd() {
      return this.position >= Latin1MemorySegmentReadingLine.this.size();
    }

    @Override
    public String readAllAsString() throws IOException {
      this.closedCheck();
      String line = Latin1MemorySegmentReadingLine.this.readAllAsString(this.position);
      this.position = Latin1MemorySegmentReadingLine.this.size();
      return line;
    }

    @Override
    public List<String> readAllLines() throws IOException {
      return List.of(this.readAllAsString());
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      // actually called by ojdbc
      this.closedCheck();
      if (len == 0) {
        return 0;
      }
      Objects.checkFromIndexSize(off, len, cbuf.length);
      if (this.atEnd()) {
        return -1;
      }
      int read = Latin1MemorySegmentReadingLine.this.transferFromTo(this.position, cbuf, off, len);
      this.position += read;
      return read;
    }

    private void closedCheck() throws IOException {
      if (this.closed) {
        throw new IOException("closed Reader");
      }
    }

    @Override
    public void close() {
      this.closed = true;
    }

  }

}
