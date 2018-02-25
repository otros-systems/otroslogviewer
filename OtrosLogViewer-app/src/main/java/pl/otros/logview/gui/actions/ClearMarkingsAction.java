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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.gui.OtrosAction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Optional;

public class ClearMarkingsAction extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClearMarkingsAction.class.getName());


  public ClearMarkingsAction(OtrosApplication otrosApplication) {
    super("Clear markings", Icons.MARKINGS_CLEAR, otrosApplication);
  }

  @Override
  protected void actionPerformedHook(ActionEvent arg0) {
    long start = System.currentTimeMillis();
    Optional<LogDataTableModel> maybeDataTableModel = getOtrosApplication().getSelectedPaneLogDataTableModel();
    maybeDataTableModel.ifPresent(dataTableModel -> {
      int rowCount = dataTableModel.getRowCount();
      LOGGER.info(String.format("Started action of clearing marks, have %d rows to check.", rowCount));
      ArrayList<Integer> markedRows = new ArrayList<>();
      for (int i = 0; i < rowCount; i++) {
        if (dataTableModel.isMarked(i)) {
          markedRows.add(i);
        }
      }
      LOGGER.info(String.format("%d rows to clear marking.", markedRows.size()));
      int[] toUnmark = new int[markedRows.size()];
      for (int i = 0; i < toUnmark.length; i++) {
        toUnmark[i] = markedRows.get(i);

      }
      dataTableModel.unmarkRows(toUnmark);
      LOGGER.info(String.format("Row markings [%d] cleared in %d ms", toUnmark.length, (System.currentTimeMillis() - start)));
      if (getOtrosApplication().getStatusObserver() != null) {
        getOtrosApplication().getStatusObserver().updateStatus("Markings have been cleared");
      }
    });
  }

}
