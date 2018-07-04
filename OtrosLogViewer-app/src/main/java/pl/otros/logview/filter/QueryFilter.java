/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
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
package pl.otros.logview.filter;

import pl.otros.logview.accept.query.QueryAcceptCondition;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.api.theme.ThemeKey;

import java.util.Properties;

/**
 * Use query to filter log events
 *
 */
public class QueryFilter extends AbstractStringBasedFilter {

  private Theme theme;
  private QueryAcceptCondition queryAcceptCondition = null;


  public QueryFilter() {
    super("Query filter", "Query Filter - see manual", "Enter query");
  }

  @Override
  public void init(Properties properties, LogDataTableModel logDataTableModel, Theme theme) {
    this.theme = theme;
  }

  @Override
  public boolean accept(LogData logData, int row) {
    if (queryAcceptCondition != null) {
      return queryAcceptCondition.accept(logData);
    }
    return true;
  }

  @Override
  protected void performPreFiltering() {
    try {
      queryAcceptCondition = new QueryAcceptCondition(getFilteringText());
    } catch (Exception e) {
      textField.setBackground(theme.getColor(ThemeKey.TEXT_FIELD_ERROR));
    }

  }

}
