package com.github.marschall.fixedlenghtfile.configuration.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class InterfaceVersionTests {

  @Test
  void ignoreZeros() {
    InterfaceVersion oneDotZero = new InterfaceVersion("1.0");
    InterfaceVersion oneDotZeroDotZero = new InterfaceVersion("1.0.0");
    InterfaceVersion oneDotZeroZero = new InterfaceVersion("1.00");

    assertThat(oneDotZero).isEqualByComparingTo(oneDotZeroDotZero);
    assertThat(oneDotZero).isEqualByComparingTo(oneDotZeroZero);

    assertEquals(oneDotZero, oneDotZeroDotZero);
    assertEquals(oneDotZero, oneDotZeroZero);

    assertThat(oneDotZero).hasSameHashCodeAs(oneDotZeroDotZero);
    assertThat(oneDotZero).hasSameHashCodeAs(oneDotZeroZero);
  }

  @Test
  void preserveZeros() {
    InterfaceVersion oneDotZero = new InterfaceVersion("1.0");
    InterfaceVersion oneDotZeroDotZero = new InterfaceVersion("1.0.0");
    InterfaceVersion oneDotZeroZero = new InterfaceVersion("1.00");

    assertThat(oneDotZero).hasToString("1.0.0");
    assertThat(oneDotZeroDotZero).hasToString("1.0.0");
    assertThat(oneDotZeroZero).hasToString("1.0.0");
  }

  @Test
  void compare() {
    List<String> unparsed = List.of("0.9", "0.9.1", "1.0", "1.0.1");

    var versionsOrdered = unparsed.stream()
                                  .map(InterfaceVersion::new)
                                  .toList();

    for (int i = 0; i < versionsOrdered.size(); i++) {
      InterfaceVersion version = versionsOrdered.get(i);

      for (int j = 0; j < i; j++) {
        InterfaceVersion smallerVersion = versionsOrdered.get(j);
        assertThat(version).isGreaterThan(smallerVersion);
      }

      assertThat(version).isEqualByComparingTo(version);

      for (int j = i + 1; j < versionsOrdered.size(); j++) {
        InterfaceVersion greaterVersion = versionsOrdered.get(j);
        assertThat(version).isLessThan(greaterVersion);
      }
    }
  }

  @Test
  void compareUnsigned() {
    List<String> unparsed = List.of("0", "1", "126", "127", "128", "255");

    //formatter:off
    var versionsOrdered = unparsed.stream()
                                   .map(InterfaceVersion::new)
                                   .toList();
    //formatter:on

    for (int i = 0; i < versionsOrdered.size(); i++) {
      InterfaceVersion version = versionsOrdered.get(i);

      for (int j = 0; j < i; j++) {
        InterfaceVersion smallerVersion = versionsOrdered.get(j);
        assertThat(version).isGreaterThan(smallerVersion);
      }

      assertThat(version).isEqualByComparingTo(version);

      for (int j = i + 1; j < versionsOrdered.size(); j++) {
        InterfaceVersion greaterVersion = versionsOrdered.get(j);
        assertThat(version).isLessThan(greaterVersion);
      }
    }
  }

  @Test
  void patchVersions() {
    InterfaceVersion four = new InterfaceVersion("4.0.0");
    InterfaceVersion fourDotOne = new InterfaceVersion("4.0.1");

    assertThat(four).isLessThan(fourDotOne);
    assertThat(fourDotOne).isGreaterThan(four);
  }

  @Test
  void testHashCode() {
    InterfaceVersion version = new InterfaceVersion("127.128.127");

    assertEquals(147997, version.hashCode(), "hashCode");
  }
  
  @Test
  void workingVersion() {
    InterfaceVersion working2 = new InterfaceVersion("2.28.0.w.2");
    assertThat(working2).hasToString("2.28.0");
  }

  @Test
  void draftVersion() {
    InterfaceVersion darft3 = new InterfaceVersion("2.40.0.d.3");
    assertThat(darft3).hasToString("2.40.0");
  }

  @Test
  void finalVersion() {
    InterfaceVersion final2 = new InterfaceVersion("2.36.0.f.2");
    assertThat(final2).hasToString("2.36.0");
  }
  
  @Test
  void finalb() {
    InterfaceVersion finalB = new InterfaceVersion("2.22.0 final b");
    assertThat(finalB).hasToString("2.22.0");
  }

}