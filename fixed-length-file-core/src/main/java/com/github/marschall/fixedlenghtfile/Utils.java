package com.github.marschall.fixedlenghtfile;

final class Utils {

  private Utils() {
    throw new AssertionError("not instantiable");
  }
  
  static short toPositiveShortExact(int i) {
    if (i == 0) {
      throw new IllegalArgumentException();
    }
    if ((i & 0xFFFF_8000) != 0) {
      throw new IllegalArgumentException();
    }
    return (short) i;
  }

}
