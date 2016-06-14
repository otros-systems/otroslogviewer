/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.gui.actions;

import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.pluginable.LogFilter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public abstract class FocusOnThisAbstractAction<T extends LogFilter> extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(FocusOnThisAbstractAction.class.getName());

  protected T filter;
  protected JCheckBox filterEnableCheckBox;

  public FocusOnThisAbstractAction(T filter, JCheckBox filterEnableCheckBox, OtrosApplication otrosApplication) {
    super(otrosApplication);
    this.filter = filter;
    this.filterEnableCheckBox = filterEnableCheckBox;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Optional<JXTable> maybeTable = getOtrosApplication().getSelectPaneJXTable();

    final Optional<LogDataTableModel> maybeTableModel = getOtrosApplication().getSelectedPaneLogDataTableModel();
    maybeTable.ifPresent(jxTable -> maybeTableModel.ifPresent(tableModel -> {
      int[] selectedRows = jxTable.getSelectedRows();
      if (selectedRows.length <= 0) {
        return;
      }

      LogData[] selectedLogData = new LogData[selectedRows.length];
      for (int i = 0; i < selectedRows.length; i++) {
        selectedLogData[i] = tableModel.getLogData(jxTable.convertRowIndexToModel(selectedRows[i]));
      }
      try {
        action(e, filter, selectedLogData);
        filterEnableCheckBox.setSelected(true);
        filter.setEnable(true);
      } catch (Exception e1) {
        LOGGER.error("Error occurred when focusing on events ", e1);
        JOptionPane.showMessageDialog(getOtrosApplication().getApplicationJFrame(), e1.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }));
  }

  public abstract void action(ActionEvent e, T filter, LogData... selectedLogData) throws Exception;
}
