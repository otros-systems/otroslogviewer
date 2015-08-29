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

import pl.otros.logview.LogData;
import pl.otros.logview.gui.HasIcon;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.LogDataTableModel;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class SelectedClassAcceptCondition extends SelectionAwareAcceptCondition implements HasIcon {

    protected Set<String> classes;

    public SelectedClassAcceptCondition(JTable jTable, LogDataTableModel dataTableModel) {
        super(jTable, dataTableModel);
    }

    @Override
    protected void init() {
        name = "Selected classes";
        classes = new HashSet<>();
    }

    public boolean accept(LogData data) {
        return classes.contains(data.getClazz());
    }


    @Override
    protected void updateAfterSelection() {
        classes.clear();
        int[] selectedRows = jTable.getSelectedRows();
        for (int i : selectedRows) {
            LogData logData = dataTableModel.getLogData(jTable.convertRowIndexToModel(i));
            classes.add(logData.getClazz());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(classes.size()).append(" classes: ");
        for (String aClass : classes) {
            int lastDot = aClass.lastIndexOf('.');
            sb.append(aClass.substring(Math.max(0,lastDot+1)));
            sb.append(", ");
        }
        sb.setLength(sb.length()-2);
        description = sb.toString();
        name = description;
        if (name.length()>NAME_LIMIT){
            name = name.substring(0,NAME_LIMIT-3) + "...";
        }
    }

    @Override
    public Icon getIcon() {
        return Icons.CLASS;
    }
}
