package com.github.marschall.fixedlenghtfile.ui;

import javax.swing.table.AbstractTableModel;

import com.github.marschall.fixedlenghtfile.FixedLengthFile;
import com.github.marschall.fixedlenghtfile.RecordDefinition.SegmentedRecordDefinition;

public class FixedLengthTableModel extends AbstractTableModel {

  private final SegmentedRecordDefinition recordDefintion;
  private final int columnCount;
  private int rowCount;

  public FixedLengthTableModel(SegmentedRecordDefinition recordDefintion) {
    this.recordDefintion = recordDefintion;
    this.columnCount = recordDefintion.getTotalFieldCount();
    this.rowCount = 0;
  }
  
  public void loadFile(int recordCount, FixedLengthFile file) {
    this.rowCount = recordCount;
    this.fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    return this.rowCount;
  }
  
  @Override
  public String getColumnName(int column) {
    // TODO Auto-generated method stub
    return super.getColumnName(column);
  }
  
  @Override
  public int findColumn(String columnName) {
    // TODO Auto-generated method stub
    return super.findColumn(columnName);
  }
  
  @Override
  public Class<?> getColumnClass(int columnIndex) {
    // TODO Auto-generated method stub
    return super.getColumnClass(columnIndex);
  }

  @Override
  public int getColumnCount() {
    // TODO Auto-generated method stub
    return this.getColumnCount();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    // TODO Auto-generated method stub
    return null;
  }

}
