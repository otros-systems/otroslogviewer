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
package pl.otros.logview.gui.actions.table;

import org.jdesktop.swingx.JXTable;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.LogDataTableModel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class MarkRowBySpaceKeyListener extends KeyAdapter {

  private final OtrosApplication otrosApplication;

  public MarkRowBySpaceKeyListener(OtrosApplication otrosApplication) {
    super();
    this.otrosApplication = otrosApplication;
  }

  @Override
  public void keyReleased(KeyEvent e) {
    char keyChar = e.getKeyChar();
    if (keyChar == ' ') {
      markUnmarkRow();
    }
  }

  private void markUnmarkRow() {
    Optional<LogDataTableModel> maybeDataTableModel = otrosApplication.getSelectedPaneLogDataTableModel();
    Optional<JXTable> maybeTable = otrosApplication.getSelectPaneJXTable();

    maybeTable.ifPresent(table -> maybeDataTableModel.ifPresent(dataTableModel -> {
        int[] selected = table.getSelectedRows();
        if (selected.length == 0) {
          return;
        }
        boolean modeMark = !dataTableModel.isMarked(selected[0]);

        if (modeMark) {
          dataTableModel.markRows(otrosApplication.getSelectedMarkColors(), selected);
        } else {
          dataTableModel.unmarkRows(selected);
        }
      }
    ));


  }

}
