package com.github.marschall.fixedlengthfile;

import java.util.Arrays;

public final class CharArrayAbstractWritingLine extends AbstractWritingLine {
  
  private final char[] buffer;

  public CharArrayAbstractWritingLine(int length) {
    this.buffer = new char[length + 2]; // for CR LF
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
