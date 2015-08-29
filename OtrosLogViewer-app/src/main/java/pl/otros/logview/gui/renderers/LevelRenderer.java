/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.renderers;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.logging.Level;

import static pl.otros.logview.gui.Icons.*;

public class LevelRenderer implements TableCellRenderer, ListCellRenderer {

  private static final Dimension ICON_DIMENSION = new Dimension(16, 16);
  private static final Color colorSevere = Color.RED;
  private static final Color colorWarning = Color.ORANGE;
  private static final Color colorInfo = Color.GREEN;
  private static final Color colorConfig = new Color(154, 255, 154);
  private static final Color colorFine = new Color(204, 204, 204);
  private static final Color colorFiner = new Color(170, 170, 170);
  private static final Color colorFinest = new Color(136, 136, 136);
  private final JLabel label = new JLabel();
  private Mode mode = Mode.IconsOnly;

  public LevelRenderer() {
    label.setOpaque(true);
  }

  public LevelRenderer(Mode mode) {
    this();
    this.mode = mode;
  }

  public static Color getColorByLevel(Level level) {
    Color result = Color.WHITE;
    if (level.equals(Level.SEVERE)) {
      result = colorSevere;
    } else if (level.equals(Level.WARNING)) {
      result = colorWarning;
    } else if (level.equals(Level.INFO)) {
      result = colorInfo;
    } else if (level.equals(Level.CONFIG)) {
      result = colorConfig;
    } else if (level.equals(Level.FINE)) {
      result = colorFine;
    } else if (level.equals(Level.FINER)) {
      result = colorFiner;
    } else if (level.equals(Level.FINEST)) {
      result = colorFinest;
    }
    return result;
  }

  public static Icon getIconByLevel(Level level) {
    Icon result = LEVEL_INFO;
    if (level.equals(Level.SEVERE)) {
      result = LEVEL_SEVERE;
    } else if (level.equals(Level.WARNING)) {
      result = LEVEL_WARNING;
    } else if (level.equals(Level.INFO)) {
      result = LEVEL_INFO;
    } else if (level.equals(Level.CONFIG)) {
      result = LEVEL_CONFIG;
    } else if (level.equals(Level.FINE)) {
      result = LEVEL_FINE;
    } else if (level.equals(Level.FINER)) {
      result = LEVEL_FINER;
    } else if (level.equals(Level.FINEST)) {
      result = LEVEL_FINEST;
    }
    return result;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof Level) {
      Level level = (Level) value;
      label.setIcon(null);
      label.setText("");
      if (!(mode == Mode.IconsOnly)) {
        label.setText(level.getName());
        label.setBackground(getColorByLevel(level));
      }
      if (!(mode == Mode.TextOnly)) {
        label.setIcon(getIconByLevel(level));
      }
      if (mode == Mode.IconsOnly) {
        label.setPreferredSize(ICON_DIMENSION);
        label.setBackground(isSelected ? DefaultColors.SELECTED : DefaultColors.BACKGROUND);
      }
    } else {
      label.setBackground(Color.WHITE);
      label.setText("");
    }
    return label;
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    label.setForeground(Color.BLACK);
    if (value instanceof Level) {
      Level level = (Level) value;
      label.setBackground(getColorByLevel(level));
      label.setText(level.getName());
      label.setIcon(getIconByLevel(level));
    } else {
      label.setBackground(Color.WHITE);
      label.setText("");
    }
    if (isSelected) {
      Color c1 = label.getBackground();
      label.setBackground(label.getForeground());
      label.setForeground(c1);
    }
    return label;

  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }


  public enum Mode {
    IconsOnly("Icons only"), TextOnly("Text"), IconsAndText("Icon and text");
    String text;

    Mode(String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
