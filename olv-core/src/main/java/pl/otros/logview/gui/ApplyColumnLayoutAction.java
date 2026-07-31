package pl.otros.logview.gui;

import org.apache.commons.configuration2.DataConfiguration;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.TableColumns;
import pl.otros.swing.table.ColumnLayout;
import pl.otros.swing.table.TablesUtils;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class ApplyColumnLayoutAction extends AbstractAction {
  private final Logger LOGGER = LoggerFactory.getLogger(ApplyColumnLayoutAction.class.getName());
  private final ColumnLayout columnLayout;
  private final JXTable table;
  private final DataConfiguration configuration;


  public ApplyColumnLayoutAction(String name, Icon icon, ColumnLayout columnLayout, JXTable table, DataConfiguration configuration) {
    super(String.format("Switch layout to \"%s\"%s", name, Objects.equals(name, configuration.getString(ConfKeys.LOG_TABLE_COLUMN_LAYOUT)) ? " (current)" : ""), icon);
    this.columnLayout = columnLayout;
    this.table = table;
    this.configuration = configuration;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    List<String> colNames = columnLayout.getColumns();
    LOGGER.debug("Retrieved {} col names: <<{}>>", colNames.size(), colNames);
    Map<String, TableColumns> colNameToEnum = new HashMap<>();
    for (TableColumns tcEnum : TableColumns.values()) {
      colNameToEnum.put(tcEnum.getName(), tcEnum);
    }

    for (TableColumn tableColumn : table.getColumns()) {
      Object o = tableColumn.getIdentifier();
      if (!(o instanceof TableColumns tcs)) {
        LOGGER.error("TableColumn identifier of unexpected type: {}", tableColumn.getIdentifier().getClass().getName());
        return;
      }
      table.getColumnExt(tcs).setVisible(false);
    }
    TablesUtils.showOnlyThisColumns(table, colNames.stream().map(colNameToEnum::get).toArray(TableColumns[]::new));
    TablesUtils.sortColumnsInOrder(columnLayout, table);
    configuration.setProperty(ConfKeys.LOG_TABLE_COLUMN_LAYOUT, columnLayout.getName());
    LOGGER.debug("Column changes applied");

  }
}
