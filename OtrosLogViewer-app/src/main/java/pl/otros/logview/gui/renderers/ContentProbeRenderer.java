package pl.otros.logview.gui.renderers;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.io.ContentProbe;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ContentProbeRenderer extends DefaultTableCellRenderer {

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (value instanceof ContentProbe) {
      ContentProbe cont = (ContentProbe) value;
      final String s = cont.getBytes().length > 0 ? new String(cont.getBytes()) : " -- empty --";
      ((JLabel) component).setText(s);
      final String[] split = StringUtils.split(s, '\n');
      String tooltip = Arrays.stream(split).collect(Collectors.joining("<br>","<html>Content:<br>","</html>"));
      ((JLabel) component).setToolTipText(tooltip);
    }
    return component;
  }
}
