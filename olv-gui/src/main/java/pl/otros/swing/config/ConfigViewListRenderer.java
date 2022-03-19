package pl.otros.swing.config;

import javax.swing.*;
import java.awt.*;

public class ConfigViewListRenderer extends DefaultListCellRenderer {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    String text = "";
    if (value instanceof ConfigView) {
      ConfigView cv = (ConfigView) value;
      text = cv.getName();
    }
    return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
  }
}
