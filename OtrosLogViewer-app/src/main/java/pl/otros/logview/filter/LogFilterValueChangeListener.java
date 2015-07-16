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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.StatusObserver;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.Collection;

public class LogFilterValueChangeListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogFilterValueChangeListener.class.getName());
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
    LOGGER.trace("Value of filter have changed, updating view");
    int selectedRow = table.getSelectedRow();
    if (selectedRow >= 0) {
      lastKnownSelectedRow = table.convertRowIndexToModel(selectedRow);
    }

    LOGGER.trace("Last selected row is {}", selectedRow);

    ArrayList<LogFilter> enabledFiltersList = new ArrayList<LogFilter>();
    for (LogFilter logFilter : logFilters) {
      if (logFilter.isEnable()) {
        enabledFiltersList.add(logFilter);
        LOGGER.trace("Filter \"{}\" is in use", logFilter.getName());
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
      LOGGER.trace("Last selected row was {} (view index: {})", selectedRow, convertRowIndexToView);
      table.scrollRectToVisible(table.getCellRect(convertRowIndexToView, 0, false));
    }

  }
}
