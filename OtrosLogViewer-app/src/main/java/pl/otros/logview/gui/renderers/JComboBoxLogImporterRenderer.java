package pl.otros.logview.gui.renderers;

import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.vfs.browser.i18n.Messages;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class JComboBoxLogImporterRenderer implements ListCellRenderer<LogImporter> {
  private final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();
  private String fileNotParsable;


  @Override
  public Component getListCellRendererComponent(JList<? extends LogImporter> list, LogImporter value, int index, boolean isSelected, boolean cellHasFocus) {
    final Component component = defaultListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (component instanceof JLabel) {
      JLabel jLabel = (JLabel) component;
      final Optional<LogImporter> optional = Optional.ofNullable(value);
      jLabel.setText(optional.map(LogImporter::getName).orElse(fileNotParsable != null ? Messages.getMessage("parser.error", fileNotParsable) : " - "));
      jLabel.setIcon(optional.isPresent() ? Icons.STATUS_OK : fileNotParsable != null ? Icons.STATUS_ERROR : null);
    }
    return component;
  }

  public void setFileNotParsable(String fileNotParsable) {
    this.fileNotParsable = fileNotParsable;
  }
}
