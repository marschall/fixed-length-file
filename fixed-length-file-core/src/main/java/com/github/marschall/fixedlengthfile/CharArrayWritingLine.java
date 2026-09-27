package com.github.marschall.fixedlengthfile;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;

public final class CharArrayWritingLine extends AbstractWritingLine {
  
  private static final char CR = '\r';
  private static final char LF = '\n';
  
  private final char[] buffer;
  private int length;

  public CharArrayWritingLine(int length) {
    this.buffer = new char[length + 2]; // for CR LF
    this.length = -1;
  }
  
  @Override
  protected void doSetLength(int recordLength) {
    this.length = recordLength;
  }

  void exportTo(Writer writer) throws IOException {
    if (this.length == -1) {
      throw new IllegalArgumentException("line has not yet been initialize");
    }
    // append CR LF
    this.buffer[this.length] = CR;
    this.buffer[this.length + 1] = LF;
    writer.write(this.buffer, 0, this.length + 2);
  }

  @Override
  void writePaddingNumber(int offset, int padding) {
    Objects.checkIndex(offset + padding - 1, this.length);
    Arrays.fill(this.buffer, offset, offset + padding, '0');
  }
  
  @Override
  void writePaddingString(int offset, int padding) {
    Objects.checkIndex(offset + padding - 1, this.length);
    Arrays.fill(this.buffer, offset, offset + padding, ' ');
  }
  
  @Override
  void writeCharAt(int index, char c) {
    Objects.checkIndex(index, this.length);
    this.buffer[index] = c;
  }

}
