package com.github.marschall.fixedlengthfile;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import com.github.marschall.fixedlengthfile.RecordDefinition.FixedLengthRecordDefinition;

public final class CharArrayWritingLine extends AbstractWritingLine {
  
  private final char[] buffer;
  private int length;

  public CharArrayWritingLine(int length) {
    this.buffer = new char[length + 2]; // for CR LF
    this.length = -1;
  }

  void initializeFor(FixedLengthRecordDefinition recordDefinition) {
    this.length = recordDefinition.getBaseLength();
    super.initializeFor(recordDefinition);
  }
  
  void exportTo(Writer writer) throws IOException {
    if (this.length == -1) {
      throw new IllegalArgumentException("line has not yet been initialize");
    }
    writer.write(this.buffer, 0, this.length);
  }

  @Override
  void writePaddingNumber(int offset, int padding) {
    // TODO range check
    Arrays.fill(this.buffer, offset, offset + padding, '0');
  }
  
  @Override
  void writePaddingString(int offset, int padding) {
    // TODO range check
    Arrays.fill(this.buffer, offset, offset + padding, ' ');
  }
  
  @Override
  void writeCharAt(int index, char c) {
    // TODO range check
    this.buffer[index] = c;
  }

}
