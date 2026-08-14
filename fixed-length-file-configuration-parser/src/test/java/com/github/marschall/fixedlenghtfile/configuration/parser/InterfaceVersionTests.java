package com.github.marschall.fixedlenghtfile.configuration.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class InterfaceVersionTests {

  @Test
  void ignoreZeros() {
    InterfaceVersion oneDotZero = InterfaceVersion.parse("1.0");
    InterfaceVersion oneDotZeroDotZero = InterfaceVersion.parse("1.0.0");
    InterfaceVersion oneDotZeroZero = InterfaceVersion.parse("1.00");

    assertThat(oneDotZero).isEqualByComparingTo(oneDotZeroDotZero);
    assertThat(oneDotZero).isEqualByComparingTo(oneDotZeroZero);

    assertEquals(oneDotZero, oneDotZeroDotZero);
    assertEquals(oneDotZero, oneDotZeroZero);

    assertThat(oneDotZero).hasSameHashCodeAs(oneDotZeroDotZero);
    assertThat(oneDotZero).hasSameHashCodeAs(oneDotZeroZero);
  }

  @Test
  void preserveZeros() {
    InterfaceVersion oneDotZero = InterfaceVersion.parse("1.0");
    InterfaceVersion oneDotZeroDotZero = InterfaceVersion.parse("1.0.0");
    InterfaceVersion oneDotZeroZero = InterfaceVersion.parse("1.00");

    assertThat(oneDotZero).hasToString("1.0.0");
    assertThat(oneDotZeroDotZero).hasToString("1.0.0");
    assertThat(oneDotZeroZero).hasToString("1.0.0");
  }

  @Test
  void compare() {
    List<String> unparsed = List.of("0.9", "0.9.1", "1.0", "1.0.1");

    var versionsOrdered = unparsed.stream()
                                  .map(InterfaceVersion::parse)
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
                                   .map(InterfaceVersion::parse)
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
    InterfaceVersion four = InterfaceVersion.parse("4.0.0");
    InterfaceVersion fourDotOne = InterfaceVersion.parse("4.0.1");

    assertThat(four).isLessThan(fourDotOne);
    assertThat(fourDotOne).isGreaterThan(four);
  }

  @Test
  void testHashCode() {
    InterfaceVersion version = InterfaceVersion.parse("127.128.127");

    assertEquals(147997, version.hashCode(), "hashCode");
  }

  @Test
  void workingVersion() {
    InterfaceVersion working2 = InterfaceVersion.parse("2.28.0.w.2");
    assertThat(working2).hasToString("2.28.0");
  }

  @Test
  void draftVersion() {
    InterfaceVersion darft3 = InterfaceVersion.parse("2.40.0.d.3");
    assertThat(darft3).hasToString("2.40.0");
  }

  @Test
  void finalVersion() {
    InterfaceVersion final2 = InterfaceVersion.parse("2.36.0.f.2");
    assertThat(final2).hasToString("2.36.0");
  }

  @Test
  void finalb() {
    InterfaceVersion finalB = InterfaceVersion.parse("2.22.0 final b");
    assertThat(finalB).hasToString("2.22.0");
  }

  @Test
  void revisionOrdering() {
    InterfaceVersion workingVersion = InterfaceVersion.parse("2.28.0.w.3");
    InterfaceVersion draftVersion = InterfaceVersion.parse("2.28.0.d.2");
    InterfaceVersion finalVersion = InterfaceVersion.parse("2.28.0.f.1");

    assertThat(workingVersion).isLessThan(draftVersion);
    assertThat(workingVersion).isLessThan(finalVersion);

    assertThat(draftVersion).isGreaterThan(workingVersion);
    assertThat(draftVersion).isLessThan(finalVersion);

    assertThat(finalVersion).isGreaterThan(draftVersion);
    assertThat(finalVersion).isGreaterThan(workingVersion);
  }

  @Test
  void toInterfaceString() {
    assertEquals("236", InterfaceVersion.parse("2.36.0.f.2").toInterfaceString());
    assertEquals("206", InterfaceVersion.parse("2.6.0.f.2").toInterfaceString());
  }

}