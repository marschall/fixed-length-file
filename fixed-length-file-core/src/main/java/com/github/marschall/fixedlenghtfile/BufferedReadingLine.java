package com.github.marschall.fixedlenghtfile;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlenghtfile.SegmentOffsets.ArrayBasedSegmentOffsets;
import com.github.marschall.fixedlenghtfile.SegmentOffsets.NoSegment;

public final class BufferedReadingLine extends AbstractReadingLine implements CharSequence {
  // ojdbc does not implement transferTo
  // implement CharSequence for integer and long parsing

  private final FileDefinition fileDefinition;
  // StringBuilder would safe half the memory for ISO-8859-1 but does not allow bulk transfer from Reader
  private final char[] buffer;
  private final int maximumPrefixLength;
  private int length;
  private ArrayBasedSegmentOffsets segmentOffsets;

  public BufferedReadingLine(FileDefinition fileDefinition) {
    this.fileDefinition = fileDefinition;
    this.maximumPrefixLength = this.fileDefinition.getMaximumPrefixLength();
    this.buffer = new char[fileDefinition.getMaximumLength()];
  }

  public void initializeFrom(Reader reader) throws IOException {
    int position = 0;
    while (position < this.buffer.length) {
      int read = reader.read(this.buffer, position, this.buffer.length - position);
      if (read != -1) {
        position += read;
      } else {
        break;
      }
    }
    String prefix = new String(this.buffer, 0, this.maximumPrefixLength);
    RecordDefinition recordDefinition = this.fileDefinition.getRecordDefinitionFromPrefix(prefix);
    // TODO determine record length
  }
  
  @Override
  protected SegmentOffsets getSegmentOffsets() {
    return Objects.requireNonNullElse(this.segmentOffsets, NoSegment.INSTANCE);
  }

  @Override
  public int readUnsignedInt(UnsignedFieldDefinition field) {
    return readUnsignedInt(0, field);
  }
  
  @Override
  protected int readUnsignedInt(int segmentStart, UnsignedFieldDefinition field) {
    // TODO sign check
    int beginIndex = segmentStart + field.getOffset();
    int endIndex = beginIndex + field.getLength();
    return Integer.parseInt(this, beginIndex, endIndex, 10);
  }

  @Override
  public long readUnsignedLong(UnsignedFieldDefinition field) {
    return readUnsignedLong(0, field);
  }
  
  @Override
  protected long readUnsignedLong(int segmentStart, UnsignedFieldDefinition field) {
    // TODO sign check
    int beginIndex = segmentStart + field.getOffset();
    int endIndex = beginIndex + field.getLength();
    return Long.parseLong(this, beginIndex, endIndex, 10);
  }
  
  private void boundsCheck(StringFieldDefinition fieldDefinition) {
    int offset = fieldDefinition.getOffset();
    int length = fieldDefinition.getLength();
    if (offset + length > this.length) {
      throw new IndexOutOfBoundsException();
    }
  }

  @Override
  public String readTrimmedString(StringFieldDefinition field) {
    this.boundsCheck(field);
    int offset = field.getOffset();
    int length = field.getLength();
    if (offset + length > this.length) {
      throw new IndexOutOfBoundsException();
    }
    // initialize with end in case string is all spaces
    int start = offset + length;
    for (int i = 0; i < length; i++) {
      char c = this.buffer[offset + i];
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
      char c = this.buffer[l];
      if (c != ' ') {
        end = l;
        break;
      }
    }
    return new String(this.buffer, start, end - start);
  }
  
  @Override
  protected String readTrimmedString(int segmentStart, StringFieldDefinition field) {
    this.boundsCheck(field);
    int offset = segmentStart + field.getOffset();
    int length = field.getLength();
    if (offset + length > this.length) {
      throw new IndexOutOfBoundsException();
    }
    // initialize with end in case string is all spaces
    int start = offset + length;
    for (int i = 0; i < length; i++) {
      char c = this.buffer[offset + i];
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
      char c = this.buffer[l];
      if (c != ' ') {
        end = l;
        break;
      }
    }
    return new String(this.buffer, start, end - start);
  }

  @Override
  public SegmentIndicator readSegmentIndicator(StringFieldDefinition field) {
    char c = this.charAt(field.getOffset());
    return toSegmentIndicator(c);
  }

  @Override
  public int getLength() {
    return this.length;
  }

  @Override
  public Reader asReader() {
    // unfortunately synchronized
    return new CharArrayReader(this.buffer, 0, this.length);
  }
  
  // CharSequence methods

  @Override
  public int length() {
    return this.length;
  }

  @Override
  public char charAt(int index) {
    return this.buffer[Objects.checkIndex(index, this.length)];
  }

  @Override
  public boolean isEmpty() {
    return this.length == 0;
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    Objects.checkFromToIndex(start, end, this.length);
    return new String(this.buffer, start, end - start);
  }

  @Override
  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    Objects.checkFromToIndex(srcBegin, srcEnd, this.length());
    Objects.checkIndex(dstBegin, dst.length - (srcEnd - srcBegin) + 1);
    System.arraycopy(this.buffer, srcBegin, dst, dstBegin, srcEnd - srcBegin);
  }

  // no code point methods as we do not expect code point access

}
