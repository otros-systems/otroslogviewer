/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
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
package pl.otros.logview.filter;

import pl.otros.logview.LogData;
import pl.otros.logview.accept.query.QueryAcceptCondition;

import java.awt.*;

/**
 * Use query to filter log events
 * 
 */
public class QueryFilter extends AbstractStringBasedFilter {

  public QueryFilter() {
    super("Query filter", "Query Filter - see manual", "Enter query");
  }

  QueryAcceptCondition queryAcceptCondition = null;

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
      textField.setBackground(Color.RED);

    }

  }

}
