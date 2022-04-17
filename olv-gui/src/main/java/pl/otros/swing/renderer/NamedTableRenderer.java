package pl.otros.swing.renderer;

import pl.otros.swing.Named;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class NamedTableRenderer implements TableCellRenderer {

  private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    final Component component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (value instanceof Named && component instanceof JLabel) {
      Named na = (Named) value;
      JLabel label = (JLabel) component;
      label.setText(na.getName());
    }
    return component;
  }
}
