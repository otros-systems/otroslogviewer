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

import com.google.common.base.Joiner;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.LogDataTableModel;

import javax.swing.*;
import java.util.Set;
import java.util.TreeSet;

public class SelectedThreadAcceptCondition extends SelectionAwareAcceptCondition {

    protected Set<String> threads;

    public SelectedThreadAcceptCondition(JTable jTable, LogDataTableModel dataTableModel) {
        super(jTable, dataTableModel);
    }

    @Override
    protected void init() {
        name = "Selected thread";
        description = name;
        threads = new TreeSet<>();
    }

    @Override
    public boolean accept(LogData data) {
        return threads.contains(data.getThread());
    }

    @Override
    protected void updateAfterSelection() {
        threads.clear();
        int[] selectedRows = jTable.getSelectedRows();
        for (int i : selectedRows) {
            LogData logData = dataTableModel.getLogData(jTable.convertRowIndexToModel(i));
            threads.add(logData.getThread());
        }

        description = threads.size()+" threads: " + Joiner.on(", ").join(threads);
        name = description;
        if (name.length()>NAME_LIMIT){
            name = name.substring(0,NAME_LIMIT-3) + "...";
        }
    }
}
