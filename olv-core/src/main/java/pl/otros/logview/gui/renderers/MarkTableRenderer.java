/*
 * Copyright 2012 Krzysztof Otrebski
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
 */
package pl.otros.logview.gui.renderers;

import pl.otros.logview.api.model.MarkerColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MarkTableRenderer extends DefaultTableCellRenderer {

  private final JCheckBox checkBox = new JCheckBox();
  private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

  public MarkTableRenderer() {
    //show the background of the JPannel
    checkBox.setOpaque(false);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    checkBox.setSelected(value instanceof MarkerColors);

    //If the checkbox is returned, then setBackground set the color of the checkbox.
    panel.add(checkBox);
    panel.setSize(checkBox.getSize());

    return panel;
  }

}
