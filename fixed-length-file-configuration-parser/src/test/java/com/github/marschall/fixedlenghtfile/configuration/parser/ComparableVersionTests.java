package com.github.marschall.fixedlenghtfile.configuration.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ComparableVersionTests {

  @Test
  void ignoreZeros() {
    ComparableVersion oneDotZero = new ComparableVersion("1.0");
    ComparableVersion oneDotZeroDotZero = new ComparableVersion("1.0.0");
    ComparableVersion oneDotZeroZero = new ComparableVersion("1.00");

    assertThat(oneDotZero).isEqualByComparingTo(oneDotZeroDotZero);
    assertThat(oneDotZero).isEqualByComparingTo(oneDotZeroZero);

    assertEquals(oneDotZero, oneDotZeroDotZero);
    assertEquals(oneDotZero, oneDotZeroZero);

    assertThat(oneDotZero).hasSameHashCodeAs(oneDotZeroDotZero);
    assertThat(oneDotZero).hasSameHashCodeAs(oneDotZeroZero);
  }

  @Test
  void preserveZeros() {
    ComparableVersion oneDotZero = new ComparableVersion("1.0");
    ComparableVersion oneDotZeroDotZero = new ComparableVersion("1.0.0");
    ComparableVersion oneDotZeroZero = new ComparableVersion("1.00");

    assertThat(oneDotZero).hasToString("1.0.0");
    assertThat(oneDotZeroDotZero).hasToString("1.0.0");
    assertThat(oneDotZeroZero).hasToString("1.0.0");
  }

  @Test
  void compare() {
    List<String> unparsed = List.of("0.9", "0.9.1", "1.0", "1.0.1");

    var versionsOrdered = unparsed.stream()
                                  .map(ComparableVersion::new)
                                  .toList();

    for (int i = 0; i < versionsOrdered.size(); i++) {
      ComparableVersion version = versionsOrdered.get(i);

      for (int j = 0; j < i; j++) {
        ComparableVersion smallerVersion = versionsOrdered.get(j);
        assertThat(version).isGreaterThan(smallerVersion);
      }

      assertThat(version).isEqualByComparingTo(version);

      for (int j = i + 1; j < versionsOrdered.size(); j++) {
        ComparableVersion greaterVersion = versionsOrdered.get(j);
        assertThat(version).isLessThan(greaterVersion);
      }
    }
  }

  @Test
  void compareUnsigned() {
    List<String> unparsed = List.of("0", "1", "126", "127", "128", "255");

    //formatter:off
    var versionsOrdered = unparsed.stream()
                                   .map(ComparableVersion::new)
                                   .toList();
    //formatter:on

    for (int i = 0; i < versionsOrdered.size(); i++) {
      ComparableVersion version = versionsOrdered.get(i);

      for (int j = 0; j < i; j++) {
        ComparableVersion smallerVersion = versionsOrdered.get(j);
        assertThat(version).isGreaterThan(smallerVersion);
      }

      assertThat(version).isEqualByComparingTo(version);

      for (int j = i + 1; j < versionsOrdered.size(); j++) {
        ComparableVersion greaterVersion = versionsOrdered.get(j);
        assertThat(version).isLessThan(greaterVersion);
      }
    }
  }

  @Test
  void patchVersions() {
    ComparableVersion four = new ComparableVersion("4.0.0");
    ComparableVersion fourDotOne = new ComparableVersion("4.0.1");

    assertThat(four).isLessThan(fourDotOne);
    assertThat(fourDotOne).isGreaterThan(four);
  }

  @Test
  void testHashCode() {
    ComparableVersion version = new ComparableVersion("127.128.127");

    assertEquals(147997, version.hashCode(), "hashCode");
  }

}