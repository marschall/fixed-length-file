package com.github.marschall.fixedlengthfile.ui;

import java.io.Serializable;

import com.github.marschall.fixedlengthfile.FieldDefinition.OffsetFieldDefinition;
import com.github.marschall.fixedlengthfile.ReadingLine;

final class ColumnModel implements Serializable {

  private final ValueAccessor valueAccessor;
  private final Class<?> columnClass;
  // TODO not Serializable
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
  interface ValueAccessor extends Serializable {

    Object readValueFrom(ReadingLine line);

  }

}
