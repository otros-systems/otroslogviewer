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
package pl.otros.logview.uml;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class LogViewTableWithUMLSynchronizer implements AdjustmentListener, ListSelectionListener {

  private final JTable table;
  private final LogUmlMapper logUmlMapper;
  private final UMLModel umlModel;

  public LogViewTableWithUMLSynchronizer(JTable table2, UMLModel umlModel) {
    this.table = table2;
    this.umlModel = umlModel;
    logUmlMapper = umlModel.getLogUmlMapper();
    table.getSelectionModel().addListSelectionListener(this);

    JScrollPane sp = umlModel.getScrollPane();
    JScrollBar sb = sp.getVerticalScrollBar();
    sb.addAdjustmentListener(this);
    umlModel.getScrollPane().getVerticalScrollBar().addAdjustmentListener(this);

  }

  public void adjustmentValueChanged(AdjustmentEvent e) {
    if (e.getSource() == umlModel.getScrollPane().getVerticalScrollBar()) {
      JScrollBar bar = (JScrollBar) e.getSource();
      int row = logUmlMapper.getLogId(bar.getValue());
      Rectangle r = table.getCellRect(row, 0, true);
      table.scrollRectToVisible(r);

    } else if (e.getSource() == table) {
    }

  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    int row = table.getSelectedRow();
    Integer o = (Integer) table.getValueAt(row, 0);
    Point p = logUmlMapper.getPoint(o);
    if (p != null) {
      Rectangle r = new Rectangle(logUmlMapper.getPoint(o));
      umlModel.getContentJComponet().scrollRectToVisible(r);
    }

  }

}
