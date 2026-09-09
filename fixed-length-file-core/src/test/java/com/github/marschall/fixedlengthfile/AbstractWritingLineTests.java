package com.github.marschall.fixedlengthfile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AbstractWritingLineTests {

  @Test
  void digits_int() {
    assertEquals(1, AbstractWritingLine.digits(0));
    assertEquals(1, AbstractWritingLine.digits(1));
    assertEquals(1, AbstractWritingLine.digits(9));

    assertEquals(2, AbstractWritingLine.digits(10));
    assertEquals(2, AbstractWritingLine.digits(99));

    assertEquals(3, AbstractWritingLine.digits(100));
    assertEquals(3, AbstractWritingLine.digits(101));
    assertEquals(3, AbstractWritingLine.digits(999));

    assertEquals(4, AbstractWritingLine.digits(1000));
    assertEquals(4, AbstractWritingLine.digits(1001));
    assertEquals(4, AbstractWritingLine.digits(9999));

    assertEquals(7, AbstractWritingLine.digits(1_000_000));
    assertEquals(7, AbstractWritingLine.digits(9_999_999));

    assertEquals(8, AbstractWritingLine.digits(10_000_000));
    assertEquals(8, AbstractWritingLine.digits(99_999_999));

    assertEquals(9, AbstractWritingLine.digits(100_000_000));
    assertEquals(9, AbstractWritingLine.digits(900_000_000));
    assertEquals(10, AbstractWritingLine.digits(Integer.MAX_VALUE));
  }

  @Test
  void digits_long() {
    assertEquals(1, AbstractWritingLine.digits(0L));
    assertEquals(1, AbstractWritingLine.digits(1L));
    assertEquals(1, AbstractWritingLine.digits(9L));

    assertEquals(2, AbstractWritingLine.digits(10L));
    assertEquals(2, AbstractWritingLine.digits(99L));

    assertEquals(3, AbstractWritingLine.digits(100L));
    assertEquals(3, AbstractWritingLine.digits(101L));
    assertEquals(3, AbstractWritingLine.digits(999L));

    assertEquals(4, AbstractWritingLine.digits(1_000L));
    assertEquals(4, AbstractWritingLine.digits(1_001L));
    assertEquals(4, AbstractWritingLine.digits(9_999L));

    assertEquals(7, AbstractWritingLine.digits(1_000_000L));
    assertEquals(7, AbstractWritingLine.digits(9_999_999L));

    assertEquals(8, AbstractWritingLine.digits(10_000_000L));
    assertEquals(8, AbstractWritingLine.digits(99_999_999L));

    assertEquals(9, AbstractWritingLine.digits(100_000_000L));
    assertEquals(9, AbstractWritingLine.digits(999_999_999L));
    assertEquals(10, AbstractWritingLine.digits((long) Integer.MAX_VALUE));

    assertEquals(11, AbstractWritingLine.digits(10_000_000_000L));
    assertEquals(11, AbstractWritingLine.digits(99_999_999_999L));
    
    assertEquals(19, AbstractWritingLine.digits(Long.MAX_VALUE));
  }

}
