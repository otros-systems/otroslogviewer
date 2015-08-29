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

import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.StatusObserver;
import pl.otros.logview.gui.markers.AutomaticMarker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AutomaticMarkUnamrkActionListener implements ActionListener {

  public static final boolean MODE_MARK = true;
  public static final boolean MODE_UNMARK = false;

  private LogDataTableModel dataTableModel;
  private AutomaticMarker automaticMarker;
  private boolean mode = true;

  private final StatusObserver observer;

  public AutomaticMarkUnamrkActionListener(LogDataTableModel dataTableModel, AutomaticMarker automaticMarker, boolean mode, StatusObserver observer) {
    super();
    this.dataTableModel = dataTableModel;
    this.automaticMarker = automaticMarker;
    this.mode = mode;
    this.observer = observer;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    int size = dataTableModel.getRowCount();
    ArrayList<Integer> rows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      LogData l = dataTableModel.getLogData(i);
      if (automaticMarker.toMark(l)) {
        rows.add(i);
      }
    }
    int[] r = new int[rows.size()];
    for (int i = 0; i < r.length; i++) {
      r[i] = rows.get(i);
    }

    if (mode == MODE_MARK) {
      dataTableModel.markRows(automaticMarker.getColors(), r);

      observer.updateStatus(r.length + " rows marked by marker \"" + automaticMarker.getName() + "\"");
    } else {
      dataTableModel.unmarkRows(r);
      observer.updateStatus(r.length + " rows unmarked by marker \"" + automaticMarker.getName() + "\"");
    }
  }

  public LogDataTableModel getDataTableModel() {
    return dataTableModel;
  }

  public void setDataTableModel(LogDataTableModel dataTableModel) {
    this.dataTableModel = dataTableModel;
  }

  public AutomaticMarker getAutomaticMarker() {
    return automaticMarker;
  }

  public void setAutomaticMarker(AutomaticMarker automaticMarker) {
    this.automaticMarker = automaticMarker;
  }

  public boolean isMode() {
    return mode;
  }

  public void setMode(boolean mode) {
    this.mode = mode;
  }

}
