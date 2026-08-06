package com.github.marschall.fixedlenghtfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UtilsTests {

  @Test
  void toPositiveShortExact_valid() {
    assertEquals(0, Utils.toPositiveShortExact(0));
    assertEquals(1, Utils.toPositiveShortExact(1));
    assertEquals(Short.MAX_VALUE - 1, Utils.toPositiveShortExact(Short.MAX_VALUE - 1));
    assertEquals(Short.MAX_VALUE, Utils.toPositiveShortExact(Short.MAX_VALUE));
  }

  @Test
  void toPositiveShortExact_invalid() {
    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(-1));

    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Short.MAX_VALUE + 1));
    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Short.MIN_VALUE + 1));
    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Short.MIN_VALUE));
    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Short.MIN_VALUE - 1));

    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Integer.MAX_VALUE));
    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Integer.MAX_VALUE - 1));
    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Integer.MIN_VALUE + 1));
    assertThrows(IllegalArgumentException.class, () -> Utils.toPositiveShortExact(Integer.MAX_VALUE));
  }

}
