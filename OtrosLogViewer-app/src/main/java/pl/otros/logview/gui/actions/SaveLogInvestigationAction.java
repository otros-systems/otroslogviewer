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
package pl.otros.logview.gui.actions;

import org.jdesktop.swingx.JXTable;
import pl.otros.logview.LogInvestiagionPersitanceUtil;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.LogDataTableModel.Memento;
import pl.otros.logview.gui.LogViewPanelWrapper;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.StatusObserver;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SaveLogInvestigationAction extends OtrosAction {


    public SaveLogInvestigationAction(OtrosApplication otrosApplication) {
        super(otrosApplication);
        putValue(Action.NAME, "Save log investigation");
        putValue(Action.SMALL_ICON, Icons.EXPORT);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        //TODO get one file chooser
        JFileChooser chooser = LogInvestiagionPersitanceUtil.getFileChooser();
        int result = chooser.showSaveDialog((Component) arg0.getSource());
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        StatusObserver observer = getOtrosApplication().getStatusObserver();
        JTabbedPane jTabbedPane = getOtrosApplication().getJTabbedPane();
        String tabTitle = jTabbedPane.getTitleAt(jTabbedPane.getSelectedIndex());
        LogViewPanelWrapper lvFrame = (LogViewPanelWrapper) jTabbedPane.getSelectedComponent();
        try {
            Memento m = lvFrame.getDataTableModel().saveToMemento();

            JXTable jxTable = lvFrame.getLogViewPanel().getTable();
            int columnCount = jxTable.getColumnCount(true);
            List<TableColumn> columns = jxTable.getColumns(true);

            for (int i = 0; i < columns.size(); i++) {
                TableColumn tableColumn = columns.get(i);
                boolean visible = jxTable.getColumnExt(tableColumn.getIdentifier()).isVisible();
                if (visible) {
                    m.getVisibleColumns().add(Integer.valueOf(i));
                }
            }

            m.setName(tabTitle);
            File f = chooser.getSelectedFile();
            if (!f.getName().endsWith("zip.olv")) {
                f = new File(f.getAbsolutePath() + ".zip.olv");
            }
            LogInvestiagionPersitanceUtil.saveMemento(m, new FileOutputStream(f));
            observer.updateStatus("Log \"" + lvFrame.getName() + "\" saved.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog((Component) arg0.getSource(), "Problem with saving: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            observer.updateStatus("Log \"" + lvFrame.getName() + "\" not saved.", StatusObserver.LEVEL_ERROR);
        }

    }

}
