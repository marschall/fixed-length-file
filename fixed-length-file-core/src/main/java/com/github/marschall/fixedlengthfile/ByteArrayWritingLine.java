package com.github.marschall.fixedlengthfile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public final class ByteArrayWritingLine extends AbstractWritingLine {

  private final byte[] buffer;
  private int length;

  public ByteArrayWritingLine(int length) {
    this.buffer = new byte[length + 2]; // for CR LF
    this.length = -1;
  }

  @Override
  protected void doSetLength(int recordLength) {
    this.length = recordLength;
  }

  void exportTo(OutputStream outputStream) throws IOException {
    if (this.length == -1) {
      throw new IllegalArgumentException("line has not yet been initialize");
    }
    outputStream.write(this.buffer, 0, this.length);
  }

  @Override
  void writePaddingNumber(int offset, int padding) {
    // TODO range check
    Arrays.fill(this.buffer, offset, offset + padding, (byte) '0');
  }

  @Override
  void writePaddingString(int offset, int padding) {
    // TODO range check
    Arrays.fill(this.buffer, offset, offset + padding, (byte) ' ');
  }

  @Override
  void writeCharAt(int index, char c) {
    // TODO range check
    this.buffer[index] = (byte) c;
  }

}
