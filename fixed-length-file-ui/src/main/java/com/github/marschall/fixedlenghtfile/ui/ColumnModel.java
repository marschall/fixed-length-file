package com.github.marschall.fixedlenghtfile.ui;

import com.github.marschall.fixedlenghtfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlenghtfile.ReadingLine;

final class ColumnModel {

  private final ValueAccessor valueAccessor;
  private final Class<?> columnClass;
  private final OffsetFieldDefinition fieldDefinition;

  ColumnModel(Class<?> columnClass, ValueAccessor valueAccessor, OffsetFieldDefinition fieldDefinition) {
    this.valueAccessor = valueAccessor;
    this.columnClass = columnClass;
    this.fieldDefinition = fieldDefinition;
  }

  Class<?> getColumnClass() {
    return this.columnClass;
  }

  String getColumnName() {
    return this.fieldDefinition.getName();
  }

  Object readValueFrom(ReadingLine line) {
    return this.valueAccessor.readValueFrom(line);
  }

  int getColumnWidth() {
    return Math.max(this.fieldDefinition.getName().length(), this.fieldDefinition.getLength());
  }

  @FunctionalInterface
  interface ValueAccessor {

    Object readValueFrom(ReadingLine line);

  }

}
