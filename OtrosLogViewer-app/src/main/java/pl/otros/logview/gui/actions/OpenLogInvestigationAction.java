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
import pl.otros.logview.LogInvestiagionPersitanceUtil;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.LogDataTableModel.Memento;
import pl.otros.logview.gui.LogViewPanelWrapper;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.StatusObserver;
import pl.otros.logview.gui.table.TableColumns;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

public class OpenLogInvestigationAction extends OtrosAction {


	public OpenLogInvestigationAction(OtrosApplication otrosApplication) {
		super(otrosApplication);
		putValue(Action.NAME, "Open log investigation");
		putValue(Action.SMALL_ICON, Icons.IMPORT);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
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
			LogViewPanelWrapper panelWrapper = new LogViewPanelWrapper(memento.getName(), null, tableColumns,getOtrosApplication());
			String tabName = panelWrapper.getName();

            getOtrosApplication().addClosableTab(tabName,tabName,Icons.FOLDER_OPEN,panelWrapper,true);
			panelWrapper.getDataTableModel().restoreFromMemento(memento);
			panelWrapper.switchToContentView();
			observer.updateStatus("Log \"" + panelWrapper.getName() + "\" loaded.");
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog((Component) arg0.getSource(), "Problem with loading: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			observer.updateStatus("Log not loaded.", StatusObserver.LEVEL_ERROR);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
