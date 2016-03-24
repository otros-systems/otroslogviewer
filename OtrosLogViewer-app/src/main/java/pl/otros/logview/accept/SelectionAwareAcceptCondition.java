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
package pl.otros.logview.accept;

import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.LogDataTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public abstract class SelectionAwareAcceptCondition extends AbstractAcceptContidion implements AcceptCondition, ListSelectionListener {

    protected JTable jTable;
    protected LogDataTableModel dataTableModel;

    public SelectionAwareAcceptCondition(JTable jTable, LogDataTableModel dataTableModel) {
        super();
        this.jTable = jTable;
        this.dataTableModel = dataTableModel;
        init();
        valueChanged(new ListSelectionEvent(this, 0, 0, false));
        jTable.getSelectionModel().addListSelectionListener(this);

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (jTable.getSelectedRow() < 0 || e.getValueIsAdjusting()) {
            return;
        }
        updateAfterSelection();
    }

    protected abstract void init();

    protected abstract void updateAfterSelection();

}
