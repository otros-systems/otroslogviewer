/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.model.Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class JumpToMarkedAction extends OtrosAction {

  private final Direction direction;

  public enum Direction {
    FORWARD, BACKWARD
  }

  public JumpToMarkedAction(OtrosApplication otrosApplication, Direction direction) {
    super(otrosApplication);
    this.direction = direction;
    if (direction == Direction.FORWARD) {
      putValue(Action.NAME, "Next marked/noted");
      putValue(Action.SMALL_ICON, Icons.ARROW_DOWN_IN_BOX);
    } else {
      putValue(Action.NAME, "Previous marked/noted");
      putValue(Action.SMALL_ICON, Icons.ARROW_UP_IN_BOX);
    }

  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    JTabbedPane tabbedPane = getOtrosApplication().getJTabbedPane();
    StatusObserver observer = getOtrosApplication().getStatusObserver();
    LogViewPanelWrapper lvPanel = (LogViewPanelWrapper) tabbedPane.getSelectedComponent();
    if (lvPanel == null) {
      observer.updateStatus("Log file not open", StatusObserver.LEVEL_ERROR);
      return;

    }
    JTable table = lvPanel.getLogViewPanel().getTable();
    LogDataTableModel model = lvPanel.getDataTableModel();
    if (table.getRowCount() == 0) {
      observer.updateStatus("Empty log", StatusObserver.LEVEL_ERROR);
      return;
    }
    int startRow = table.getSelectedRow();
    startRow = startRow < 0 ? startRow = 0 : startRow;
    int nextRow = startRow;
    while (true) {
      if (direction == Direction.FORWARD) {
        nextRow++;
      } else {
        nextRow--;
      }

      if (nextRow == table.getRowCount()) {
        nextRow = 0;
      } else if (nextRow < 0) {
        nextRow = table.getRowCount() - 1;
      }

      if (nextRow == startRow) {
        // not found
        observer.updateStatus("Next marked not found", StatusObserver.LEVEL_ERROR);
        return;
      }
      observer.updateStatus("Checking row: " + nextRow);
      int rowInModel = table.convertRowIndexToModel(nextRow);
      boolean marked = model.isMarked(rowInModel);
      Note n = model.getNote(rowInModel);
      boolean haveNote = n != null && n.getNote() != null && n.getNote().length() > 0;
      if (marked || haveNote) {
        Rectangle rect = table.getCellRect(nextRow, 0, true);
        table.scrollRectToVisible(rect);
        table.clearSelection();
        table.setRowSelectionInterval(nextRow, nextRow);
        observer.updateStatus("Next marked/noted found at " + table.convertRowIndexToView(nextRow), StatusObserver.LEVEL_NORMAL);
        break;
      }

    }

  }

}
