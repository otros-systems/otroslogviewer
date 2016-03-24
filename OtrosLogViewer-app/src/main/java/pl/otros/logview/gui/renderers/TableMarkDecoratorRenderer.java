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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.MarkerColors;
import pl.otros.logview.api.MarkableTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class TableMarkDecoratorRenderer implements TableCellRenderer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TableMarkDecoratorRenderer.class.getName());
  private final TableCellRenderer subjectRenderer;


  public TableMarkDecoratorRenderer(TableCellRenderer subjectRenderer) {
    super();
    this.subjectRenderer = subjectRenderer;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = subjectRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    boolean marked = false;
    TableModel model = table.getModel();
    MarkerColors markerColors = MarkerColors.Aqua;
    if (model instanceof MarkableTableModel) {
      MarkableTableModel markedModel = (MarkableTableModel) model;
      try {
        int converterRow = table.convertRowIndexToModel(row);
        LOGGER.trace(String.format("Checking if row %d [model %d] is marked.",row, converterRow));
        marked = markedModel.isMarked(converterRow);
        LOGGER.trace(String.format("Row %d is marked: %s",row,marked));
        markerColors = markedModel.getMarkerColors(converterRow);
        LOGGER.trace(String.format("Color of row %d is %s",row,markerColors));
      } catch (NullPointerException e) {
        LOGGER.error( "TableMarkDecoratorRenderer.getTableCellRendererComponent() null pointer caught at index " + row, e);
      }
    }

    Color bg = DefaultColors.BACKGROUND;
    Color fg = DefaultColors.FOREGROUND;
    if (!isSelected && !marked) {
      bg = DefaultColors.BACKGROUND;
    } else if (isSelected && !marked) {
      bg = DefaultColors.SELECTED;
      fg = DefaultColors.SELECTED_FOREGROUND;
    } else if (marked && !isSelected) {
      // bg = DefaultColors.MARKED;
      bg = markerColors.getBackground();
      fg = markerColors.getForeground();
    } else if (marked && isSelected) {
      bg = markerColors.getBackground().darker();
      fg = markerColors.getForeground();
      float[] bgHSB = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
      if (bgHSB[2] > 0.4f) {
        bgHSB[2] -= 0.08;
      } else {
        bgHSB[2] += 0.04;
      }
      bgHSB[1] = 0.3f;
      bg = new Color(Color.HSBtoRGB(bgHSB[0], bgHSB[1], bgHSB[2]));
    }
    c.setBackground(bg);
    c.setForeground(fg);

    return c;
  }

}
