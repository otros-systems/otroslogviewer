package pl.otros.swing.functional;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.function.Function;

public class MultilineStringTableCellRenderer<E> implements TableCellRenderer {

  private DefaultTableCellRenderer renderer;
  private JTextArea textArea;
  private final Function<E, String> mapping;

  public MultilineStringTableCellRenderer(Function<E, String> mapping) {
    this.mapping = mapping;
    textArea = new JTextArea();
    renderer = new DefaultTableCellRenderer();
  }


  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    Component component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    textArea.setBackground(component.getBackground());
    textArea.setForeground(component.getForeground());
    try {
      final String text = mapping.apply((E) value);
      textArea.setText(text);
      table.setRowHeight(row,table.getRowHeight() * text.split("\n").length);
    } catch (Exception e){
      //ignore it
    }
    return textArea;
  }
}
