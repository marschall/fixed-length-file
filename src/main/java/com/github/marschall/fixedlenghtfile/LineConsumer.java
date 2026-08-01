package com.github.marschall.fixedlenghtfile;

@FunctionalInterface
public interface LineConsumer {

  void accept(String recordType, int recordNumber, ReadingLine line);

}
