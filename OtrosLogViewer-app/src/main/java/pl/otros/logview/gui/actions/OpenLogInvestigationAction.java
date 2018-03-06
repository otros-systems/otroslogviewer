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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogDataTableModel.Memento;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.persistance.LogInvestiagionPersitanceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

public class OpenLogInvestigationAction extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenLogInvestigationAction.class);

  public OpenLogInvestigationAction(OtrosApplication otrosApplication) {
    super(otrosApplication);
    putValue(Action.NAME, "Open log investigation");
    putValue(Action.SMALL_ICON, Icons.IMPORT);
  }

  @Override
  protected void actionPerformedHook(ActionEvent arg0) {
    getOtrosApplication().getServices().getStatsService().actionExecuted(this);
    StatusObserver observer = getOtrosApplication().getStatusObserver();
    //TODO get one file chooser
    JFileChooser chooser = LogInvestiagionPersitanceUtil.getFileChooser();
    int result = chooser.showOpenDialog((Component) arg0.getSource());
    if (result != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File f = chooser.getSelectedFile();
    FileInputStream in = null;
    try {
      in = new FileInputStream(f);
      Memento memento = LogInvestiagionPersitanceUtil.loadMemento(in);
      TableColumns[] tableColumns = TableColumns.values();
      if (memento.getVisibleColumns().size() > 0) {
        tableColumns = new TableColumns[memento.getVisibleColumns().size()];
        int i = 0;
        for (Integer visibleColumn : memento.getVisibleColumns()) {
          tableColumns[i] = TableColumns.getColumnById(visibleColumn);
          i++;
        }
      }
      LogViewPanelWrapper panelWrapper = new LogViewPanelWrapper(memento.getName(), null, tableColumns, getOtrosApplication());
      String tabName = panelWrapper.getName();

      getOtrosApplication().addClosableTab(tabName, tabName, Icons.FOLDER_OPEN, panelWrapper, true);
      panelWrapper.getDataTableModel().restoreFromMemento(memento);
      panelWrapper.switchToContentView();
      observer.updateStatus("Log \"" + panelWrapper.getName() + "\" loaded.");
    } catch (Exception e) {
      LOGGER.error("Can't read log investigation");
      JOptionPane.showMessageDialog((Component) arg0.getSource(), "Problem with loading: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      observer.updateStatus("Log not loaded.", StatusObserver.LEVEL_ERROR);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}
