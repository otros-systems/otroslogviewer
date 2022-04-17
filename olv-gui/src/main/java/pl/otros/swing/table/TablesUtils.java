package pl.otros.swing.table;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TablesUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(TablesUtils.class.getName());

  /**
   * Find column index by it's name
   *
   * @param columnModel column model
   * @param columnName  column name to find
   * @return column index or -1 if not found
   */
  public static Integer findColumnIndexByHeader(TableColumnModel columnModel, String columnName) {
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      final String header = columnModel.getColumn(i).getHeaderValue().toString();
      if (StringUtils.equals(header, columnName)) {
        return i;
      }
    }
    LOGGER.warn("Can't find column index for " + columnName);
    return -1;
  }

  public static void sortColumnsInOrder(ColumnLayout columnLayout, JTable table) {
    final List<String> columns = columnLayout.getColumns();
    for (int i = 0; i < columns.size(); i++) {
      final int index = findColumnIndexByHeader(table.getColumnModel(), columns.get(i));
      if (index > -1) {
        LOGGER.info("Moving " + index + " to " + i);
        table.moveColumn(index, i);
      }
    }
  }

  public static void showOnlyThisColumns(JXTable table, Object[] columns) {
    for (Object tableColumns : columns) {
      table.getColumnExt(tableColumns).setVisible(true);
    }
//    updateColumnsSize();
  }
}
