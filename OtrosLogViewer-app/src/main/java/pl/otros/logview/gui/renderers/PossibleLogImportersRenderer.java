package pl.otros.logview.gui.renderers;

import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.PossibleLogImporters;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class PossibleLogImportersRenderer extends DefaultTableCellRenderer {
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    final JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (value instanceof PossibleLogImporters) {
      PossibleLogImporters poss = (PossibleLogImporters) value;
      final String text = poss.getLogImporter().map(LogImporter::getName).orElse("N/A");
      component.setText(text);
    }
    return component;
  }
}
