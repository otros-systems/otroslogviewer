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

import pl.otros.logview.gui.LogDataTableModel;

public class SearchContext {

  private int lastRow = 0;
  private LogDataTableModel dataTableModel;
  private SearchMatcher searchMatcher;

  public SearchMatcher getSearchMatcher() {
    return searchMatcher;
  }

  public void setSearchMatcher(SearchMatcher searchMatcher) {
    this.searchMatcher = searchMatcher;
  }

  public int getLastRow() {
    return lastRow;
  }

  public void setLastRow(int lastRow) {
    this.lastRow = lastRow;
  }

  public LogDataTableModel getDataTableModel() {
    return dataTableModel;
  }

  public void setDataTableModel(LogDataTableModel dataTableModel) {
    this.dataTableModel = dataTableModel;
  }

}
