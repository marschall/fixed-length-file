package com.github.marschall.fixedlenghtfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

import java.io.IOException;
import java.io.Reader;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.BoundField.BoundBigDecimalField;
import com.github.marschall.fixedlenghtfile.BoundField.BoundLocalDateTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundIntegerField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalDateField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLocalTimeField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundLongField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundSegmentIndicatorField;
import com.github.marschall.fixedlenghtfile.BoundField.OffsetField.BoundStringField;

abstract sealed class Latin1MemorySegmentReadingLine implements ReadingLine
  permits FixedLatin1MemorySegmentReadingLine, SegmentedLatin1MemorySegmentReadingLine {

  private final MemorySegment memorySegment;

  Latin1MemorySegmentReadingLine(MemorySegment segment) {
    this.memorySegment = segment;
  }

  @Override
  public int readUnsignedInt(BoundIntegerField field) {
    return readUnsignedInt(0, field);
  }

  protected int readUnsignedInt(int baseOffset, BoundIntegerField field) {
    int offset = baseOffset + field.getOffset();
    return readUnsignedInt(offset, field.getLength());
  }
  
  private int readUnsignedInt(int offset, int length) {
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

  @Override
  public LocalDate readLocalDate(BoundLocalDateField field) {
    int yyyyMMdd = readUnsignedInt(field.getOffset(), field.getLength());
    int dayOfMonth = yyyyMMdd % 100;
    int month = (yyyyMMdd / 100) % 100;
    int year = yyyyMMdd / 100_00;
    return LocalDate.of(year, month, dayOfMonth);
  }
  
  @Override
  public LocalTime readLocalTime(BoundLocalTimeField field) {
    int length = field.getLength();
    int value = readUnsignedInt(field.getOffset(), length);
    // length 6: hhmmss 8: hhmmsscc
    int hhmmsscc = length == 6 ? value * 100 : value;
    int nanoOfSecond = (hhmmsscc % 100) * 10_000_000; // xx -> xx0_000_000
    int second = (hhmmsscc / 100) % 100;
    int minute = (hhmmsscc / 100_00) % 100;
    int hour = hhmmsscc / 100_00_00;
    return LocalTime.of(hour, minute, second, nanoOfSecond);
  }
  
  @Override
  public LocalDateTime readLocalDateTime(BoundLocalDateTimeField field) {
    var localDate = readLocalDate(field.getLocalDateField());
    var localTime = readLocalTime(field.getLocalTimeField());
    return LocalDateTime.of(localDate, localTime);
  }

  private char readCharAt(int index) {
    byte b = this.memorySegment.getAtIndex(JAVA_BYTE, index);
    return (char) Byte.toUnsignedInt(b);
  }

  private RuntimeException digitExpectedAt(int i, char c) {
    return new FileFormatException("expected digit at index: " + (this.memorySegment.address() + i) + " but got: " + c);
  }

  @Override
  public long readUnsignedLong(BoundLongField field) {
    return readUnsignedLong(0, field);
  }

  protected long readUnsignedLong(int baseOffset, BoundLongField field) {
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
  public BigDecimal readBigDecimal(BoundBigDecimalField field) {
    long amount = readUnsignedLong(field.getAmountField());
    int exponent = readUnsignedInt(field.getExponentField());
    return BigDecimal.valueOf(amount, exponent);
  }

  @Override
  public String readTrimmedString(BoundStringField field) {
    return readTrimmedString(0, field);
  }
  
  protected String readTrimmedString(int baseOffset, BoundStringField field) {
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
    return new MemorySegmentReader();
  }

  final class MemorySegmentReader extends Reader {
    // TODO mark
    // TODO transferTo

    private boolean closed;

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
