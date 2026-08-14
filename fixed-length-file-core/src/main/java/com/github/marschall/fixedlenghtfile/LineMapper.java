package com.github.marschall.fixedlenghtfile;

@FunctionalInterface
public interface LineMapper<T> {

  T map(String recordType, int recordNumber, ReadingLine line);

}
