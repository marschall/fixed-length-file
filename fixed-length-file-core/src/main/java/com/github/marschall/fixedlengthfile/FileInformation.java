package com.github.marschall.fixedlengthfile;

import java.util.Objects;

import com.github.marschall.fixedlengthfile.FileDefinition.Version;

public abstract sealed class FileInformation {

  FileInformation() {
    super();
  }

  public static abstract sealed class Versioned extends FileInformation {

    private Version version;

    Versioned(FileDefinition.Version version) {
      this.version = Objects.requireNonNull(version);
    }

    public Version getVersion() {
      return this.version;
    }

    public static final class Main extends Versioned {

      Main(Version version) {
        super(version);
      }
    }

    public static final class Variant extends Versioned {

      Variant(Version version) {
        super(version);
      }
    }

  }

  public static final class Unknown extends FileInformation {

    public static FileInformation INSTANCE = new Unknown();

    private Unknown() {
      super();
    }

  }
  

}
