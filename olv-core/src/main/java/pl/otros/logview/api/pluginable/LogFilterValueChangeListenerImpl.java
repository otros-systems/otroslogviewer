package pl.otros.logview.api.pluginable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.filter.LogFilter;
import pl.otros.logview.api.filter.LogFilterValueChangeListener;
import pl.otros.logview.api.gui.LogDataRowFilter;
import pl.otros.logview.api.gui.LogDataTableModel;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.Collection;
import java.util.List;

public class LogFilterValueChangeListenerImpl implements LogFilterValueChangeListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogFilterValueChangeListenerImpl.class.getName());
  private final TableRowSorter<LogDataTableModel> rowSorter;
  private final Collection<LogFilter> logFilters;
  private final StatusObserver observer;
  private final JTable table;
  private int lastKnownSelectedRowInModel = -1;
  private final LogDataTableModel dataTableModel;

  public LogFilterValueChangeListenerImpl(JTable table, TableRowSorter<LogDataTableModel> rowSorter, Collection<LogFilter> logFilters, StatusObserver statusObserver, LogDataTableModel dataTableModel) {
    super();
    this.table = table;
    this.rowSorter = rowSorter;
    this.logFilters = logFilters;
    this.observer = statusObserver;
    this.dataTableModel = dataTableModel;
  }

  @Override
  public void valueChanged() {
    LOGGER.trace("Value of filter have changed, updating view");
    int selectedRow = table.getSelectedRow();
    if (selectedRow >= 0) {
      lastKnownSelectedRowInModel = table.convertRowIndexToModel(selectedRow);
    }

    LOGGER.trace("Last selected row is {}", selectedRow);

    List<LogFilter> enabledFiltersList = logFilters.stream().filter(LogFilter::isEnable).toList();
    if (LOGGER.isTraceEnabled()) {
      enabledFiltersList.forEach(logFilter -> LOGGER.trace("Filter \"{}\" is in use", logFilter.getName()));
    }
    int rowCount = dataTableModel.getRowCount();
    if (rowCount > 0) {
      enabledFiltersList.forEach(logFilter -> {
        logFilter.setFirstRow(dataTableModel.getLogData(0));
        logFilter.setLastRow(dataTableModel.getLogData(rowCount - 1));
      });
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

    if (lastKnownSelectedRowInModel >= 0 && lastKnownSelectedRowInModel < table.getModel().getRowCount()) {
      int toSelectInView = table.convertRowIndexToView(lastKnownSelectedRowInModel);
      if (toSelectInView >= 0) {
        LOGGER.info("Last selected row was {} (view index: {})", selectedRow, toSelectInView);
        table.scrollRectToVisible(table.getCellRect(toSelectInView, 0, false));
      } else {
        LOGGER.info("Selected row was filtered out");
      }
    }

  }
}
