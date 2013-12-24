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
package pl.otros.logview.gui.table;

import java.util.HashMap;

public enum TableColumns {

  ID("ID", 0), //
  TIME("Time", 1), //
  LEVEL("L", 2), //
  MESSAGE("Message", 3), //
  CLASS("Class", 4), //
  METHOD("Method", 5), //
  THREAD("Thread", 6), //
  MARK("M", 7), //
  NOTE("Enter a Note", 8), //
  FILE("File", 9), //
  LINE("Line", 10), //
  NDC("NDC", 11), //
  PROPERTIES("Properties", 12), //
  LOGGER_NAME("Logger", 13), //
  LOG_SOURCE("Source", 14);//

  private static HashMap<Integer, TableColumns> map = new HashMap<Integer, TableColumns>();
  static {
    TableColumns[] values = TableColumns.values();
    for (TableColumns tableColumns : values) {
      map.put(tableColumns.getColumn(), tableColumns);
    }
  }

  public static final TableColumns[] JUL_COLUMNS = new TableColumns[] { TableColumns.CLASS,//
      TableColumns.ID,//
      TableColumns.LEVEL,//
      TableColumns.MARK,//
      TableColumns.MESSAGE,//
      TableColumns.METHOD,//
      TableColumns.NOTE,//
      TableColumns.THREAD,//
      TableColumns.TIME //

  };

  public static final TableColumns[] ALL_WITHOUT_LOG_SOURCE = new TableColumns[] { TableColumns.CLASS,//
      TableColumns.ID, //
      TableColumns.TIME, //
      TableColumns.LEVEL, //
      TableColumns.MESSAGE, //
      TableColumns.CLASS, //
      TableColumns.METHOD, //
      TableColumns.THREAD, //
      TableColumns.MARK, //
      TableColumns.NOTE, //
      TableColumns.FILE, //
      TableColumns.LINE, //
      TableColumns.NDC, //
      TableColumns.PROPERTIES, //
      TableColumns.LOGGER_NAME //

  };

  private String name;
  private int column;

  private TableColumns(String name, int column) {
    this.name = name;
    this.column = column;
  }

  public String getName() {
    return name;
  }

  public int getColumn() {
    return column;
  }

  public static TableColumns getColumnById(int id) {
    return map.get(id);
  }
}
