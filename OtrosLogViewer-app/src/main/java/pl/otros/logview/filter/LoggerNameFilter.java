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

public class LoggerNameFilter extends AbstractStringBasedFilter {

  private static final String NAME = "Logger name filter";
  private static final String DESCRIPTION = "Filtering events based on a Logger name.";
  private static final String LABEL_TEXT = "Enter text";
  private String condition = "";

  public LoggerNameFilter() {
    super(NAME, DESCRIPTION, LABEL_TEXT);
  }

  @Override
  public boolean accept(LogData logData, int row) {
    String loggerName = logData.getLoggerName();
    if (loggerName == null || loggerName.length() == 0) {
      return false;
    } else if (condition.length() == 0) {
      return true;
    } else if (isIgnoreCase()) {
      loggerName = loggerName.toLowerCase();
    }
    return loggerName.equals(condition);
  }

  @Override
  protected void performPreFiltering() {
    condition = isIgnoreCase() ? getFilteringText().toLowerCase() : getFilteringText();
  }

  public void setLoggerNameFilterAndGuiChange(String loggerName) {
    textField.setText(loggerName);
  }
}
