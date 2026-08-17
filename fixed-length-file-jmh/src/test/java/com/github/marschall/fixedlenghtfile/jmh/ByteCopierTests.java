package com.github.marschall.fixedlenghtfile.jmh;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.Test;

class ByteCopierTests {

  @Test
  void copyScalar() {
    byte[] latin1 = latin1Bytes(256);
    char[] output = new char[latin1.length];
    assertEquals(latin1.length, ByteCopier.copyScalar(MemorySegment.ofArray(latin1), output, 0, latin1.length));
    assertArrayEquals(expected(latin1), output);
  }

  @Test
  void copySwar() {
    byte[] latin1 = latin1Bytes(256);
    char[] output = new char[latin1.length];
    MemorySegment heapSegment = MemorySegment.ofArray(latin1);
    try (Arena arena = Arena.ofConfined()) {

      MemorySegment nativeSegment = arena.allocate(latin1.length);
      nativeSegment.copyFrom(heapSegment);

      assertEquals(latin1.length, ByteCopier.copySwar(nativeSegment, output, 0, latin1.length));
      assertArrayEquals(expected(latin1), output);
    }
  }

  private static byte[] latin1Bytes(int length) {
    byte[] bytes = new byte[length];
    for (int i = 0; i < length; i++) {
      bytes[i] = (byte) i;
    }
    return bytes;
  }

  private static char[] expected(byte[] bytes) {
    return new String(bytes, ISO_8859_1).toCharArray();
  }

}
