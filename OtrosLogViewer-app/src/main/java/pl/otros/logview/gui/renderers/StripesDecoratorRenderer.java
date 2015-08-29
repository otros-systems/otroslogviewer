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

public class StripesDecoratorRenderer implements TableCellRenderer {

  private final TableCellRenderer subjectRenderer;

  public StripesDecoratorRenderer(TableCellRenderer subjectRenderer) {
    super();
    this.subjectRenderer = subjectRenderer;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = subjectRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (row % 2 == 0) {
      // c.
      Color color = c.getBackground();
      float shift = 0.1f;
      float[] colorHSB = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      // if (colorHSB[2] > 0.5f) {
      // shift = shift * -1;
      // }
      colorHSB[2] += shift;
      color = new Color(Color.HSBtoRGB(colorHSB[0], colorHSB[1], colorHSB[2]));
      c.setBackground(color);
    }

    return c;
  }

}
