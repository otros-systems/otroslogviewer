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

import org.jdesktop.swingx.JXTable;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.gui.OtrosAction;

import java.awt.event.ActionEvent;
import java.util.Optional;

public class UnMarkRowAction extends OtrosAction {


  public UnMarkRowAction(OtrosApplication otrosApplication) {
    super(otrosApplication);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Optional<JXTable> maybeTable = getOtrosApplication().getSelectPaneJXTable();
    Optional<LogDataTableModel> maybeModel = getOtrosApplication().getSelectedPaneLogDataTableModel();

    maybeTable.ifPresent(table -> maybeModel.ifPresent(model -> {
      StatusObserver observer = getOtrosApplication().getStatusObserver();
      int[] selected = table.getSelectedRows();
      for (int i = 0; i < selected.length; i++) {
        selected[i] = table.convertRowIndexToModel(selected[i]);
      }
      model.unmarkRows(selected);
      if (observer != null) {
        observer.updateStatus(selected.length + " rows unmarked");
      }
    }));
  }

}
