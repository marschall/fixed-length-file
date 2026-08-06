package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.io.IOException;
import java.io.Reader;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.BoundField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundStringField;

final class Latin1MemorySegmentReadingLine implements ReadingLine {

  private final MemorySegment segment;

  Latin1MemorySegmentReadingLine(MemorySegment segment, long start) {
    this(segment);
  }

  Latin1MemorySegmentReadingLine(MemorySegment segment) {
    this.segment = segment;
  }

  @Override
  public int readUnsignedInt(BoundIntegerField field) {
    int offset = field.getOffset();
    int length = field.getLength();
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

  private char readCharAt(long index) {
    byte b = this.segment.getAtIndex(JAVA_BYTE, index);
    return (char) Byte.toUnsignedInt(b);
  }

  private static RuntimeException digitExpectedAt(long i, char c) {
    return new FileFormatException("expected digit at index: " + i + " but got: " + c);
  }

  @Override
  public long readUnsignedLong(BoundLongField field) {
    int offset = field.getOffset();
    int length = field.getLength();
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
  public String readTrimmedString(BoundStringField field) {
    int offset = field.getOffset();
    int length = field.getLength();
    long start = offset + length - 1L;
    for (int i = 0; i < length; i++) {
      char c = readCharAt(offset + i);
      if (c != ' ') {
        start = offset + i;
        break;
      }
    }
    if (start == offset + length - 1L) {
      // avoid allocation for the common case of an empty string
      return "";
    }
    long end = offset + length - 1L;
    for (long l = end; l >= start; l--) {
      char c = readCharAt(l);
      if (c != ' ') {
        end = l;
        break;
      }
    }
    int bufferLength = (int) (end - start) + 1;
    byte[] buffer = new byte[bufferLength];
    MemorySegment.copy(this.segment, JAVA_BYTE, start, buffer, 0, bufferLength);
    // REVIEW this.segment.asSlice().getString() would avoid one copy
    return new String(buffer, 0, bufferLength, ISO_8859_1);
  }

  String readAllAsString(int start) {
    int bufferLength = size() - start;
    byte[] buffer = new byte[bufferLength];
    MemorySegment.copy(this.segment, JAVA_BYTE, start, buffer, 0, bufferLength);
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
    return Math.toIntExact(this.segment.byteSize());
  }

  @Override
  public SegmentIndicator readSegmentIndicator(BoundSegmentIndicatorField field) {
    int offset = field.getOffset();
    char c = readCharAt(offset);
    return switch (c) {
      case SegmentIndicator.PRESENT_VALUE -> SegmentIndicator.PRESENT;
      case SegmentIndicator.ABSENT_VALUE -> SegmentIndicator.ABSENT;
      case SegmentIndicator.SPACES_VALUE -> SegmentIndicator.SPACES;
      default -> throw new FileFormatException("Unexpected segment indicator: " + c);
    };
  }

  @Override
  public int getLength() {
    return size();
  }

  @Override
  public Reader asReader() {
    return new SegmentReader();
  }

  final class SegmentReader extends Reader {
    // TODO mark

    private boolean closed;

    private int position;

    SegmentReader() {
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
