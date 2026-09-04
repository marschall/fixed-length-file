package com.github.marschall.fixedlengthfile.configuration.parser;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

final class Helper {

  private Helper() {
    throw new AssertionError("not instantiable");
  }

  static Path findFileIn(Path basePath, String subString, String exentsion) throws IOException {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(basePath, "*" + subString + "*." + exentsion)) {
      for (Path path : directoryStream) {
        return path;
      }
    }
    throw new NoSuchElementException("no file with extension: " + exentsion + " found in: " + basePath);
  }

}
