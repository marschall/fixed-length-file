package com.github.marschall.fixedlenghtfile.configuration.parser;
/**
 * A version made up of up to 3 integer components separated by {@code '.'}
 * and an optional revision preceeded by {@code '.'},.
 */
final class InterfaceVersion implements Comparable<InterfaceVersion> {

  private static final int MAX_VALUE = Byte.toUnsignedInt((byte) -1);

  private final byte major;
  private final byte minor;
  private final byte micro;

  /**
   * Constructs a new {@link InterfaceVersion}.
   *
   * @param version the version string made up of up to 3 positive integer components separated by {@code '.'}
   *                and an optional revision preceeded by {@code '.'},
   *                not {@code null}
   */
  InterfaceVersion(String version) {
    int end = version.indexOf('.');
    if (end == -1) {
      this.major = parseByte(version, 0, version.length());
      this.minor = 0;
      this.micro = 0;
    } else {
      this.major = parseByte(version, 0, end);
      int start = end + 1;
      end = version.indexOf('.', start);
      if (end == -1) {
        this.minor = parseByte(version, start, version.length());
        this.micro = 0;
      } else {
        this.minor = parseByte(version, start, end);
        start = end + 1;
        end = findMicroEnd(version, start);
        this.micro = parseByte(version, start, end);
      }
    }
  }
  
  private static int findMicroEnd(String version, int start) {
    int dotEnd = version.indexOf('.', start);
    int spaceEnd = version.indexOf(' ', start);
    if (dotEnd == -1 && spaceEnd != -1) {
      return spaceEnd;
    }
    if (spaceEnd == -1 && dotEnd != -1) {
      return dotEnd;
    }
    if (spaceEnd == -1 && dotEnd == -1) {
      return version.length();
    }
    return Math.min(dotEnd, spaceEnd);
  }

  private static byte parseByte(String s, int beginIndex, int endIndex) {
    return toByte(Integer.parseInt(s, beginIndex, endIndex, 10));
  }

  private static byte toByte(int i) {
    if (i < 0) {
      throw new IllegalArgumentException("negative numbers not supported");
    }
    if (i > MAX_VALUE) {
      throw new IllegalArgumentException("version component too large");
    }
    return (byte) i;
  }

  @Override
  public int compareTo(InterfaceVersion o) {
    int result = Byte.compareUnsigned(this.major, o.major);
    if (result != 0) {
      return result;
    }
    result = Byte.compareUnsigned(this.minor, o.minor);
    if (result != 0) {
      return result;
    }
    return Byte.compareUnsigned(this.micro, o.micro);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof InterfaceVersion)) {
      return false;
    }
    InterfaceVersion other = (InterfaceVersion) obj;
    return (this.major == other.major)
            && (this.minor == other.minor)
            && (this.micro == other.micro);
  }

  @Override
  public int hashCode() {
    return ((((31 + this.major) * 31) + this.minor) * 31) + this.micro;
  }

  @Override
  public String toString() {
    return Integer.toString(Byte.toUnsignedInt(major))
        + '.' + Integer.toString(Byte.toUnsignedInt(minor))
        + '.' + Integer.toString(Byte.toUnsignedInt(micro));
  }

}