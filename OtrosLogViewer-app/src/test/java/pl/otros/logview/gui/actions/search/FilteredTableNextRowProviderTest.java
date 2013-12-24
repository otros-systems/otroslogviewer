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

import org.junit.Test;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import static org.junit.Assert.assertEquals;

public class FilteredTableNextRowProviderTest {

  @Test
  public void testGetNextRowDown() {
    JTable jTable = new JTable(10, 1);
    NextRowProvider nextRowProvider = NextRowProviderFactory.getNextFilteredTableRow(jTable);
    for (int i = 0; i < 10; i++) {
      assertEquals(i, nextRowProvider.getNextRow());
    }
    assertEquals(-1, nextRowProvider.getNextRow());
  }

  @Test
  public void testGetNextRowFromMiddleDown() {
    JTable jTable = new JTable(10, 1);
    jTable.getSelectionModel().setSelectionInterval(4, 4);
    NextRowProvider nextRowProvider = NextRowProviderFactory.getNextFilteredTableRow(jTable);
    for (int i = 5; i < 10; i++) {
      assertEquals(i, nextRowProvider.getNextRow());
    }
    for (int i = 0; i < 5; i++) {
      assertEquals(i, nextRowProvider.getNextRow());
    }
    assertEquals(-1, nextRowProvider.getNextRow());
  }

  @Test
  public void testGetNextRowUp() {
    JTable jTable = new JTable(10, 1);
    NextRowProvider nextRowProvider = NextRowProviderFactory.getPreviousFilteredTableRow(jTable);
    for (int i = 9; i >= 0; i--) {
      int nextRow = nextRowProvider.getNextRow();

      // assertEquals(i, nextRow);
    }
    assertEquals(-1, nextRowProvider.getNextRow());
  }

  @Test
  public void testGetNextRowFromMiddleUp() {
    JTable jTable = new JTable(10, 1);
    jTable.getSelectionModel().setSelectionInterval(4, 4);
    NextRowProvider nextRowProvider = NextRowProviderFactory.getPreviousFilteredTableRow(jTable);
    for (int i = 3; i >= 0; i--) {
      assertEquals(i, nextRowProvider.getNextRow());
    }
    for (int i = 9; i > 3; i--) {
      assertEquals(i, nextRowProvider.getNextRow());
    }
    assertEquals(-1, nextRowProvider.getNextRow());
  }

  @Test
  public void testFilteredTable() {
    JTable jTable = new JTable(10, 1);
    TableRowSorter rowSorter = new TableRowSorter(jTable.getModel());
    jTable.setRowSorter(rowSorter);
    RowFilter<TableModel, Integer> rowFilter = new RowFilter<TableModel, Integer>() {

      public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        return entry.getIdentifier().intValue() % 2 == 0;
      }
    };

    rowSorter.setRowFilter(rowFilter);
    NextRowProvider nextRowProvider = NextRowProviderFactory.getNextFilteredTableRow(jTable);
    for (int i = 0; i < 5; i++) {
      int nextRow = nextRowProvider.getNextRow();
      assertEquals(i * 2, nextRow);
    }
    assertEquals(-1, nextRowProvider.getNextRow());
  }
}
