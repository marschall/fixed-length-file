package com.github.marschall.fixedlengthfile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public final class ByteArrayWritingLine extends AbstractWritingLine {

  private final byte CR = 13;
  private final byte LF = 10;

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
    // append CR LF
    this.buffer[this.length] = CR;
    this.buffer[this.length + 1] = LF;
    outputStream.write(this.buffer, 0, this.length);
  }

  @Override
  void writePaddingNumber(int offset, int padding) {
    Objects.checkIndex(offset + padding - 1, this.length);
    Arrays.fill(this.buffer, offset, offset + padding, (byte) '0');
  }

  @Override
  void writePaddingString(int offset, int padding) {
    Objects.checkIndex(offset + padding - 1, this.length);
    Arrays.fill(this.buffer, offset, offset + padding, (byte) ' ');
  }

  @Override
  void writeCharAt(int index, char c) {
    Objects.checkIndex(index, this.length);
    this.buffer[index] = (byte) c;
  }

}
