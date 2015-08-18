package pl.otros.logview.gui;

import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.table.TableColumns;
import pl.otros.swing.table.ColumnLayout;
import pl.otros.swing.table.TablesUtils;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ApplyColumnLayoutAction extends AbstractAction {
  private final Logger LOGGER = LoggerFactory.getLogger(ApplyColumnLayoutAction.class.getName());
  private final ColumnLayout columnLayout;
  private final JXTable table;

  public ApplyColumnLayoutAction(String name, Icon icon, ColumnLayout columnLayout, JXTable table) {
    super(String.format("Switch layout to \"%s\"", name), icon);
    this.columnLayout = columnLayout;
    this.table = table;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    List<String> colNames = columnLayout.getColumns();
    LOGGER.debug(String.format("Retrieved %d col names: <<%s>>", colNames.size(), colNames.toString()));
    List<TableColumns> visCols = new ArrayList<>();
    Map<String, TableColumns> colNameToEnum = new HashMap<>();
    for (TableColumns tcEnum : TableColumns.values()) {
      colNameToEnum.put(tcEnum.getName(), tcEnum);
    }

    for (TableColumn tableColumn : table.getColumns()) {
      Object o = tableColumn.getIdentifier();
      if (!(o instanceof TableColumns)) {
        LOGGER.error("TableColumn identifier of unexpected type: " + tableColumn.getIdentifier().getClass().getName());
        return;
      }
      TableColumns tcs = (TableColumns) o;
      table.getColumnExt(tcs).setVisible(false);
    }
    visCols.addAll(colNames.stream().map(colNameToEnum::get).collect(Collectors.toList()));
    TablesUtils.showOnlyThisColumns(table, visCols.toArray(new TableColumns[visCols.size()]));
    TablesUtils.sortColumnsInOrder(columnLayout, table);
    LOGGER.debug("Column changes applied");

  }
}
