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
package pl.otros.logview.gui.actions.search;

import javax.swing.*;

public class FilteredTableNextRowProvider implements NextRowProvider {

  private final JTable table;
  private final int increase;
  private int currentRow = 0;
  private int rowsChecked;

  public FilteredTableNextRowProvider(JTable table, SearchDirection direction) {
    super();
    this.table = table;
    int rowSearchStart = table.getRowCount();
    increase = SearchDirection.FORWARD.equals(direction) ? 1 : -1;
    if (table.getSelectedRow() >= 0) {
      rowSearchStart = table.getSelectedRow();
    }
    currentRow = rowSearchStart;
  }

  @Override
  public int getNextRow() {
    currentRow += increase;

    if (rowsChecked++ >= table.getRowCount()) {
      return -1;
    }
    if (currentRow >= table.getRowCount()) {
      currentRow = 0;
    } else if (currentRow < 0) {
      currentRow = table.getRowCount() - 1;
    }
    int convertRowIndexToModel = table.convertRowIndexToModel(currentRow);
    return convertRowIndexToModel;
  }
}
