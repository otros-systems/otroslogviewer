/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.table;

import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.pluginable.LogFilter;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.HashSet;
import java.util.Set;

public class FilteredTableModel extends AbstractTableModel {

  private final LogDataTableModel logDataTableModel;
  private final Set<LogFilter> appliedFilters;
  private int[] modelToView;
  private int[] viewToModel;

  public FilteredTableModel(LogDataTableModel logDataTableModel) {
    super();
    this.logDataTableModel = logDataTableModel;
    appliedFilters = new HashSet<>();
    logDataTableModel.addTableModelListener(e -> doFiltering());
    doFiltering();
  }

  protected void doFiltering() {
    int filterRowIndex = 0;
    int rowCount = logDataTableModel.getRowCount();
    modelToView = new int[rowCount];
    viewToModel = new int[rowCount];
    for (int row = 0; row < rowCount; row++) {
      boolean accept = true;
      LogData logData = logDataTableModel.getLogData(row);
      for (LogFilter logFilter : appliedFilters) {
        accept = accept && logFilter.accept(logData, row);
      }
      if (accept) {
        modelToView[row] = filterRowIndex;
        viewToModel[filterRowIndex] = row;
        filterRowIndex++;
      } else {
        modelToView[row] = -1;
      }
    }
    int[] newViewToModel = new int[filterRowIndex];
    System.arraycopy(viewToModel, 0, newViewToModel, 0, filterRowIndex);
    viewToModel = newViewToModel;
  }

  @Override
  public int getRowCount() {
    return viewToModel.length;
  }

  @Override
  public int getColumnCount() {
    return logDataTableModel.getColumnCount();
  }

  public int convertModelToView(int row) {
    return modelToView[row];
  }

  public int convertViewToModel(int row) {
    return viewToModel[row];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return logDataTableModel.getValueAt(convertViewToModel(rowIndex), columnIndex);
  }

  public void addFilter(LogFilter filter) {
    appliedFilters.add(filter);
    filterChanged();
  }

  public void removeFilter(LogFilter filter) {
    appliedFilters.remove(filter);
    filterChanged();
  }

  public void filterChanged() {
    doFiltering();
    fireTableDataChanged();
  }

  public String getColumnName(int columnIndex) {
    return logDataTableModel.getColumnName(columnIndex);
  }

  public Class<?> getColumnClass(int columnIndex) {
    return logDataTableModel.getColumnClass(columnIndex);
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return logDataTableModel.isCellEditable(rowIndex, columnIndex);
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    logDataTableModel.setValueAt(aValue, rowIndex, columnIndex);
  }

  public void addTableModelListener(TableModelListener l) {
    logDataTableModel.addTableModelListener(l);
  }

  public void removeTableModelListener(TableModelListener l) {
    logDataTableModel.removeTableModelListener(l);
  }

}
