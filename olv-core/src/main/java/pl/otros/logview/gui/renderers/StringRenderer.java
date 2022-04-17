/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.otros.logview.gui.renderers;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Administrator
 */
public class StringRenderer implements TableCellRenderer {
  private final JLabel label;

  public StringRenderer() {
    this.label = new JLabel();
    label.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String message = value == null ? "" : value.toString();
    label.setText(StringUtils.abbreviate(message, 500).replace("\t", "    ").replace("\n", "|"));
    return label;
  }

}
