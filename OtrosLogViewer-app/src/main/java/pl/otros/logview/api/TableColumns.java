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
package pl.otros.logview.api;

import java.util.HashMap;

public enum TableColumns {

  ID("ID", 0), //
  TIME("Time", 1), //
  DELTA("Delta", 2), //
  LEVEL("L", 3), //
  MESSAGE("Message", 4), //
  CLASS("Class", 5), //
  METHOD("Method", 6), //
  THREAD("Thread", 7), //
  MARK("M", 8), //
  NOTE("Enter a Note", 9), //
  FILE("File", 10), //
  LINE("Line", 11), //
  NDC("NDC", 12), //
  PROPERTIES("Properties", 13), //
  LOGGER_NAME("Logger", 14), //
  LOG_SOURCE("Source", 15);//

  private static final HashMap<Integer, TableColumns> map = new HashMap<>();
  static {
    TableColumns[] values = TableColumns.values();
    for (TableColumns tableColumns : values) {
      map.put(tableColumns.getColumn(), tableColumns);
    }
  }

  public static final TableColumns[] JUL_COLUMNS = { TableColumns.CLASS,//
      TableColumns.ID,//
      TableColumns.LEVEL,//
      TableColumns.MARK,//
      TableColumns.MESSAGE,//
      TableColumns.METHOD,//
      TableColumns.NOTE,//
      TableColumns.THREAD,//
      TableColumns.TIME //

  };

  public static final TableColumns[] ALL_WITHOUT_LOG_SOURCE = { TableColumns.CLASS,//
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

  private final String name;
  private final int column;

  TableColumns(String name, int column) {
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
