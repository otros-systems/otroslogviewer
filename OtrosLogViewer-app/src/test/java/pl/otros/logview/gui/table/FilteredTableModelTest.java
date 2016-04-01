/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.table;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.filter.AbstractLogFilter;
import pl.otros.logview.api.gui.LogDataTableModel;

import java.awt.*;
import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;

public class FilteredTableModelTest {

  private final class TestLogFilterExtension extends AbstractLogFilter {

    private int passIndex = Integer.MAX_VALUE;

    private TestLogFilterExtension(String name, String description) {
      super(name, description);
    }

    @Override
    public Component getGUI() {
      return null;
    }

    @Override
    public boolean accept(LogData logData, int row) {
      return row < passIndex;
    }

    public void setPassIndex(int passIndex) {
      this.passIndex = passIndex;
    }

  }

  private LogDataTableModel dataTableModel;
  private FilteredTableModel filteredTableModel;

  @BeforeMethod
  public void prepare() {
    dataTableModel = new LogDataTableModel();
    for (int i = 0; i < 100; i++) {
      LogData ld = new LogData();
      ld.setId(i);
      ld.setClazz("Class");
      ld.setDate(new Date(1000 * i));
      ld.setMessage("Message " + (i % 10));
      dataTableModel.add(ld);
    }
    filteredTableModel = new FilteredTableModel(dataTableModel);
  }

  @Test
  public void testGetRowCount() {
    assertEquals(dataTableModel.getRowCount(), filteredTableModel.getRowCount());
    TestLogFilterExtension filter = new TestLogFilterExtension("A", "B");
    filteredTableModel.addFilter(filter);
    assertEquals(dataTableModel.getRowCount(), filteredTableModel.getRowCount());
    filter.setPassIndex(50);
    filteredTableModel.doFiltering();
    assertEquals(50, filteredTableModel.getRowCount());
  }

  @Test
  public void testGetColumnCount() {
    assertEquals(dataTableModel.getColumnCount(), filteredTableModel.getColumnCount());
  }

  @Test
  public void testConvertModelToView() {
    filteredTableModel.addFilter(new AbstractLogFilter("A", "B") {

      @Override
      public Component getGUI() {
        return null;
      }

      @Override
      public boolean accept(LogData logData, int row) {
        return row % 2 == 0;
      }
    });

    filteredTableModel.doFiltering();

    int rowIndex = 0;
    for (int i = 0; i < dataTableModel.getRowCount(); i++) {
      if (i % 2 == 0) {
        assertEquals(rowIndex, filteredTableModel.convertModelToView(i));
        rowIndex++;
      } else {
        assertEquals(-1, filteredTableModel.convertModelToView(i));
      }
    }

  }

  @Test
  public void testConvertViewToModel() {
    filteredTableModel.addFilter(new AbstractLogFilter("A", "B") {

      @Override
      public Component getGUI() {
        return null;
      }

      @Override
      public boolean accept(LogData logData, int row) {
        return row % 2 == 0;
      }
    });

    filteredTableModel.doFiltering();

    for (int i = 0; i < filteredTableModel.getRowCount(); i++) {
      assertEquals(i * 2, filteredTableModel.convertViewToModel(i));
    }
  }

}
