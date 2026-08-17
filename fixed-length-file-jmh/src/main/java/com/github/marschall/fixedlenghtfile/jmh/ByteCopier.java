package com.github.marschall.fixedlenghtfile.jmh;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.MemorySegment;

final class ByteCopier {

  private ByteCopier() {
    throw new AssertionError("not instantiable");
  }

  static int copyScalar(MemorySegment memorySegment, char[] cbuf, int off, int len) {
    int toRead = Math.min(Math.toIntExact(memorySegment.byteSize()), len);
    copyScalarChecked(memorySegment, cbuf, off, toRead);
    return toRead;
  }

  private static void copyScalarChecked(MemorySegment memorySegment, char[] cbuf, int off, int len) {
    for (int i = 0; i < len; i++) {
      cbuf[off + i] = readCharAt(memorySegment, i);
    }
  }

  private static char readCharAt(MemorySegment memorySegment, int index) {
    byte b = memorySegment.getAtIndex(JAVA_BYTE, index);
    return (char) Byte.toUnsignedInt(b);
  }


  static int copySwar(MemorySegment memorySegment, char[] cbuf, int off, int len) {
    int toRead = Math.min(Math.toIntExact(memorySegment.byteSize()), len);

    int preLoop = Math.toIntExact(memorySegment.address() % 8L); // TODO & 0xFF
    copyScalarChecked(memorySegment, cbuf, off, preLoop);

    if (preLoop < toRead) {
      int fullLoop = (toRead - preLoop) & ~0xFF;
      copySwar(memorySegment, fullLoop, preLoop, cbuf, off + preLoop);
      int postLoop = toRead - preLoop - fullLoop;
      copyScalarChecked(memorySegment, cbuf, off + preLoop + fullLoop, postLoop);
    }

    return toRead;
  }


  static int copySwar(MemorySegment memorySegment, int toRead, int base, char[] cbuf, int off) {
    for (int i = 0; i < toRead; i += 8) {
      //long value = memorySegment.getAtIndex(JAVA_LONG, base + i);
      long value = memorySegment.get(JAVA_LONG, base + i);
      cbuf[i] = (char) (value & 0xFFL);
      cbuf[i + 1] = (char) ((value & (0xFFL << 8)) >>> 8);
      cbuf[i + 2] = (char) ((value & (0xFFL << 16)) >>> 16);
      cbuf[i + 3] = (char) ((value & (0xFFL << 24)) >>> 24);
      cbuf[i + 4] = (char) ((value & (0xFFL << 32)) >>> 32);
      cbuf[i + 5] = (char) ((value & (0xFFL << 40)) >>> 40);
      cbuf[i + 6] = (char) ((value & (0xFFL << 48)) >>> 48);
      cbuf[i + 7] = (char) ((value & (0xFFL << 56)) >>> 56);

    }
    return toRead;
  }

}
