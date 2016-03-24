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
package pl.otros.logview.filter;

import pl.otros.logview.api.LogData;

public class StringContainsFilter extends AbstractStringBasedFilter {

  private static final String NAME = "String contains filter";
  private static final String DESCRIPTION = "Filtering events based on a matching string in log message.";
  private static final String LABLE_TEXT = "Enter text";
  private String condition = "";

  public StringContainsFilter() {
    super(NAME, DESCRIPTION, LABLE_TEXT);
  }

  @Override
  public boolean accept(LogData logData, int row) {
    String message = isIgnoreCase() ? logData.getMessage().toLowerCase() : logData.getMessage();
    return condition.length() == 0 || message.contains(condition);
  }

  @Override
  protected void performPreFiltering() {
    condition = isIgnoreCase() ? getFilteringText().toLowerCase() : getFilteringText();

  }

}
