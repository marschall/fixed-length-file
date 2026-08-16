package com.github.marschall.fixedlenghtfile.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.github.marschall.fixedlenghtfile.ReadingLine;
import com.github.marschall.fixedlenghtfile.StatefulFixedLengthFile;
import com.github.marschall.fixedlenghtfile.StatefulFixedLengthFile.LineLocator;

public class FixedLengthTableModel extends AbstractTableModel {

  private final int columnCount;
  private int rowCount;
  private List<ColumnModel> columnModelList;
  private Map<String, Integer> columnModelMap;
  private List<LineLocator> lines;
  private StatefulFixedLengthFile file;

  public FixedLengthTableModel(List<ColumnModel> columnModelList) {
    this.columnModelList = columnModelList;
    this.columnModelMap = HashMap.newHashMap(columnModelList.size());
    for (int i = 0; i < columnModelList.size(); i++) {
      this.columnModelMap.put(columnModelList.get(i).getColumnName(), i);
    }
    this.columnCount = columnModelList.size();
    this.rowCount = 0;
  }
  
  public void loadFile(StatefulFixedLengthFile file, List<LineLocator> lines) {
    this.file = file;
    this.rowCount = lines.size();
    this.lines = lines;
    this.fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    return this.rowCount;
  }
  
  @Override
  public String getColumnName(int column) {
    return this.columnModelList.get(column).getColumnName();
  }
  
  @Override
  public int findColumn(String columnName) {
    Integer columnIndex = this.columnModelMap.get(columnName);
    if (columnIndex != null) {
      return columnIndex;
    } else {
      return -1;
    }
  }
  
  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return this.columnModelList.get(columnIndex).getColumnClass();
  }

  @Override
  public int getColumnCount() {
    return this.columnCount;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    LineLocator locator = this.lines.get(rowIndex);
    ReadingLine line = this.file.readLine(locator);
    ColumnModel model = this.columnModelList.get(columnIndex);
    return model.readValueFrom(line);
  }

}
