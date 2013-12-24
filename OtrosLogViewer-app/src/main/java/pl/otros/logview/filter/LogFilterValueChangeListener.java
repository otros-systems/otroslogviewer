/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.filter;

import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.StatusObserver;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFilterValueChangeListener {

  private static final Logger LOGGER = Logger.getLogger(LogFilterValueChangeListener.class.getName());
  private TableRowSorter<LogDataTableModel> rowSorter;
  private Collection<LogFilter> logFilters;
  private StatusObserver observer;
  private final JTable table;
  private int lastKnownSelectedRow = -1;

  public LogFilterValueChangeListener(JTable table, TableRowSorter<LogDataTableModel> rowSorter, Collection<LogFilter> logFilters, StatusObserver statusObserver) {
    super();
    this.table = table;
    this.rowSorter = rowSorter;
    this.logFilters = logFilters;
    this.observer = statusObserver;
  }

  public void valueChanged() {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Value of filter have changed, updateding view");
    }
    int selectedRow = table.getSelectedRow();
    if (selectedRow >= 0) {
      lastKnownSelectedRow = table.convertRowIndexToModel(selectedRow);
    }
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest(String.format("Last selected row is %d", selectedRow));
    }

    ArrayList<LogFilter> enabledFiltersList = new ArrayList<LogFilter>();
    for (LogFilter logFilter : logFilters) {
      if (logFilter.isEnable()) {
        enabledFiltersList.add(logFilter);
        if (LOGGER.isLoggable(Level.FINEST)) {
          LOGGER.finest(String.format("Filter \"%s\" is in use", logFilter.getName()));
        }
      }
    }

    LogFilter[] enabledFilters = new LogFilter[enabledFiltersList.size()];
    enabledFilters = enabledFiltersList.toArray(enabledFilters);

    LogDataRowFilter dataRowFilter = new LogDataRowFilter(enabledFilters);
    rowSorter.setRowFilter(dataRowFilter);
    int filtered = rowSorter.getViewRowCount();
    if (observer != null) {
      if (filtered > 0) {
        observer.updateStatus(filtered + " messages passed filters");
      } else {
        observer.updateStatus("No messages passed filters", StatusObserver.LEVEL_ERROR);
      }
    }

    if (lastKnownSelectedRow >= 0 && lastKnownSelectedRow < table.getRowCount()) {
      int convertRowIndexToView = table.convertRowIndexToView(lastKnownSelectedRow);
      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.finest(String.format("Last selected row was %d (view index: %d)", selectedRow, convertRowIndexToView));
      }
      table.scrollRectToVisible(table.getCellRect(convertRowIndexToView, 0, false));
    }

  }
}
