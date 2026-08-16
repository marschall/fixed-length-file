package com.github.marschall.fixedlenghtfile.ui;

import com.github.marschall.fixedlenghtfile.ReadingLine;

final class ColumnModel {

  private final ValueAccessor valueAccessor;
  private final Class<?> columnClass;
  private final String columnName;

  ColumnModel(String columnName, Class<?> columnClass, ValueAccessor valueAccessor) {
    this.columnName = columnName;
    this.valueAccessor = valueAccessor;
    this.columnClass = columnClass;
  }

  Class<?> getColumnClass() {
    return this.columnClass;
  }

  String getColumnName() {
    return this.columnName;
  }

  Object readValueFrom(ReadingLine line) {
    return this.valueAccessor.readValueFrom(line);
  }

  @FunctionalInterface
  interface ValueAccessor {

    Object readValueFrom(ReadingLine line);

  }

}
