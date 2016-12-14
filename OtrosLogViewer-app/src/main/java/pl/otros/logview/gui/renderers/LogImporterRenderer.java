package pl.otros.logview.gui.renderers;

import pl.otros.logview.api.importer.LogImporter;

import javax.swing.*;
import java.awt.*;

public class LogImporterRenderer implements ListCellRenderer<LogImporter> {

  private DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

  @Override
  public Component getListCellRendererComponent(JList<? extends LogImporter> list, LogImporter value, int index, boolean isSelected, boolean cellHasFocus) {
    final Component component = defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (component instanceof JLabel) {
      JLabel jLabel = (JLabel) component;
      jLabel.setText(value.getName());
      jLabel.setIcon(value.getIcon());
    }
    return component;
  }
}
