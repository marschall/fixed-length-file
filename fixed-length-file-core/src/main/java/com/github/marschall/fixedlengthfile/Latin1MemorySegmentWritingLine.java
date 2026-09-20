package com.github.marschall.fixedlengthfile;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.MemorySegment;
import java.util.List;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.StringFieldDefinition;
import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition.UnsignedFieldDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;
import com.github.marschall.fixedlengthfile.RecordDefinition.SegmentedRecordDefinition;

public final class Latin1MemorySegmentWritingLine extends AbstractWritingLine {

  private MemorySegment segment;

  public Latin1MemorySegmentWritingLine(MemorySegment segment) {
    this.segment = segment;
  }

  @Override
  public void initializeFor(SegmentedRecordDefinition recordDefinition, List<SegmentIndicator> segmentIndicators) {
    super.initializeFor(recordDefinition, segmentIndicators);
  }

  @Override
  public void initializeFor(FixedLengthRecordDefinition recordDefinition) {
    super.initializeFor(recordDefinition);
  }
  
  @Override
  protected void doSetLength(int recordLength) {
    this.segment = this.segment.asSlice(0, recordLength);
  }

  @Override
  public void writeUnsignedInt(UnsignedFieldDefinition field, int value) {
    // RREVIEW other option
    // buffer = toLatin1ByteArray(value);
    // .asSlice(base, padding).fill((byte) '0');
    // MemorySegment.copy(buffer, 0, this.segment, ValueLayout.JAVA_BYTE, base + padding, buffer.length);
    super.writeUnsignedInt(field, value);
  }
  
  @Override
  public void writeString(StringFieldDefinition field, String s) {
    // REVIEW this.segment.setString will add 0 terminator
    super.writeString(field, s);
  }

  @Override
  void writePaddingString(int offset, int padding) {
    for (int i = 0; i < padding; i++) {
      // .asSlice(base, padding).fill((byte) ' ');
      writeCharAt(offset + i, ' ');
    }
  }
  
  @Override
  void writePaddingNumber(int offset, int padding) {
    for (int i = 0; i < padding; i++) {
      writeCharAt(offset + i, '0');
    }
  }

  @Override
  void writeCharAt(int index, char c) {
    byte b = (byte) c;
    this.segment.setAtIndex(JAVA_BYTE, index, b);
  }

  private static byte[] toLatin1ByteArray(int i) {
    // REVIEW could be pooled
    int digits = digits(i);
    byte[] buffer = new byte[digits];
    int remaining = i;
    for (int j = 0; j < digits; j++) {
      int digit = remaining % 10;
      buffer[digits - j - 1] = (byte) ('0' + digit);
      remaining = remaining / 10;
    }
    return buffer;
  }

}
