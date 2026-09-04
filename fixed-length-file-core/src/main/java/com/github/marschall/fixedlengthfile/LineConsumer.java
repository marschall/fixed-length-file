package com.github.marschall.fixedlengthfile;

@FunctionalInterface
public interface LineConsumer {

  void accept(String recordType, int recordNumber, ReadingLine line);

}
