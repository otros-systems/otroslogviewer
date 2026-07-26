package pl.otros.logview.gui.util;

import pl.otros.logview.api.TableColumns;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class JTableUtil {

  private JTableUtil() {
  }

  /**
   * Calculates the preferred width required to display a sample value in a table
   * column using the column's actual cell renderer.
   * <p>
   * The method creates the renderer component for the specified sample data and
   * returns its preferred width. This takes font metrics, renderer borders,
   * insets, and other renderer-specific settings into account, resulting in a
   * more accurate width calculation than using {@link java.awt.FontMetrics}
   * directly.
   *
   * @param tableColumns the column definition containing the target column index
   * @param dataExample  a representative value to be rendered in the column
   *                     (typically the longest expected value)
   * @param tableToFill  the table whose renderer should be used for the
   *                     calculation
   * @return the preferred width, in pixels, required to display the specified
   * value
   */
  public static int calculateTextWidth(TableColumns tableColumns, String dataExample, JTable tableToFill) {
    int columnIndex = tableColumns.getColumn();
    //The calculation is only possible if the column is visible
    int viewColumnIndex = tableToFill.convertColumnIndexToView(columnIndex);

    TableCellRenderer renderer = tableToFill.getCellRenderer(0, viewColumnIndex);
    Component component = renderer.getTableCellRendererComponent(
      tableToFill,
      dataExample,
      false,
      false,
      0,
      viewColumnIndex);
    return component.getPreferredSize().width;
  }

  /**
   * Generate a Date String with the widest possible date.
   * For example, "88:88:88.888", because "8" is the widest character.
   * <p>
   * This is useful if you want to calculate the width of a column.
   */
  public static String getWidestDateString(String pattern) {
    Calendar cal = Calendar.getInstance();

    cal.set(8888, Calendar.DECEMBER, 28, 23, 59, 59);
    cal.set(Calendar.MILLISECOND, 999);

    String text = new SimpleDateFormat(pattern).format(cal.getTime());

    return text.replaceAll("\\d", "8");
  }
}
