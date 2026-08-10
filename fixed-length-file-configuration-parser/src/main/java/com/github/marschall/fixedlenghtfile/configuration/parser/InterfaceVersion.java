package com.github.marschall.fixedlenghtfile.configuration.parser;
/**
 * A version made up of up to 3 integer components separated by {@code '.'}
 * and an optional revision preceeded by {@code '.'},.
 */
abstract sealed class InterfaceVersion implements Comparable<InterfaceVersion> {
  
  static final class WorkingVersion extends InterfaceVersion {

    WorkingVersion(byte major, byte minor, byte micro) {
      super(major, minor, micro);
    }

    @Override
    int getMaturityIndicator() {
      return 0;
    }

  }

  static final class DraftVersion extends InterfaceVersion {

    DraftVersion(byte major, byte minor, byte micro) {
      super(major, minor, micro);
    }

    @Override
    int getMaturityIndicator() {
      return 1;
    }

  }

  static final class FinalVersion extends InterfaceVersion {

    FinalVersion(byte major, byte minor, byte micro) {
      super(major, minor, micro);
    }

    @Override
    int getMaturityIndicator() {
      return 2;
    }

  }

  private static final int MAX_VALUE = Byte.toUnsignedInt((byte) -1);

  private final byte major;
  private final byte minor;
  private final byte micro;

  protected InterfaceVersion(byte major, byte minor, byte micro) {
    this.major = major;
    this.minor = minor;
    this.micro = micro;
  }



  /**
   * Constructs a new {@link InterfaceVersion}.
   *
   * @param version the version string made up of up to 3 positive integer components separated by {@code '.'}
   *                and an optional revision preceeded by {@code '.'},
   *                not {@code null}
   */
  static InterfaceVersion parse(String version) {
    byte major;
    byte minor;
    byte micro;

    int end = version.indexOf('.');
    if (end == -1) {
      major = parseByte(version, 0, version.length());
      minor = 0;
      micro = 0;
    } else {
      major = parseByte(version, 0, end);
      int start = end + 1;
      end = version.indexOf('.', start);
      if (end == -1) {
        minor = parseByte(version, start, version.length());
        micro = 0;
      } else {
        minor = parseByte(version, start, end);
        start = end + 1;
        end = findMicroEnd(version, start);
        micro = parseByte(version, start, end);
      }
    }
    if (version.contains(".w.")) {
      return new WorkingVersion(major, minor, micro);
    }
    if (version.contains(".d.")) {
      return new DraftVersion(major, minor, micro);
    }
    if (version.contains(".f.") || version.contains(" final ")) {
      return new FinalVersion(major, minor, micro);
    }
    return new FinalVersion(major, minor, micro);
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
    result = Byte.compareUnsigned(this.micro, o.micro);
    if (result != 0) {
      return result;
    }
    return Integer.compare(this.getMaturityIndicator(), o.getMaturityIndicator());
  }
  
  abstract int getMaturityIndicator();

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