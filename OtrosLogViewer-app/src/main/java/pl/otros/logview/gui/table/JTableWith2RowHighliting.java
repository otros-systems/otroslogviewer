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
package pl.otros.logview.gui.table;

import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Vector;

public class JTableWith2RowHighliting extends JXTable {

  public JTableWith2RowHighliting() {
    super();
  }

  public JTableWith2RowHighliting(int numRows, int numColumns) {
    super(numRows, numColumns);
  }

  public JTableWith2RowHighliting(Object[][] rowData, Object[] columnNames) {
    super(rowData, columnNames);
  }

  public JTableWith2RowHighliting(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
    super(dm, cm, sm);
  }

  public JTableWith2RowHighliting(TableModel dm, TableColumnModel cm) {
    super(dm, cm);
  }

  public JTableWith2RowHighliting(TableModel dm) {
    super(dm);
  }

  @SuppressWarnings("rawtypes")
  public JTableWith2RowHighliting(Vector rowData, Vector columnNames) {
    super(rowData, columnNames);
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component prepareRenderer = super.prepareRenderer(renderer, row, column);
    if (row % 2 == 1) {
      Color bkg = prepareRenderer.getBackground();
      Color bkgNew = new Color(Math.max( bkg.getRed() - 25, 0), Math.max( bkg.getGreen() - 25, 0), Math.max( bkg.getBlue() - 25, 0));
      prepareRenderer.setBackground(bkgNew);
    }
    return prepareRenderer;
  }
}
