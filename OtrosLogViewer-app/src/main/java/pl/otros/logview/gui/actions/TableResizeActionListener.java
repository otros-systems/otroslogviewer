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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TableResizeActionListener implements ActionListener {

  private JTable table;
  private int autoResizeMode;

  public TableResizeActionListener() {

  }

  public TableResizeActionListener(JTable table, int autoResizeMode) {
    super();
    this.table = table;
    this.autoResizeMode = autoResizeMode;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (table != null) {
      table.setAutoResizeMode(autoResizeMode);
    }

  }

  public JTable getTable() {
    return table;
  }

  public void setTable(JTable table) {
    this.table = table;
  }

  public int getAutoResizeMode() {
    return autoResizeMode;
  }

  public void setAutoResizeMode(int autoResizeMode) {
    this.autoResizeMode = autoResizeMode;
  }

}
