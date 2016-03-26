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

import pl.otros.logview.api.model.LogData;

import java.awt.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexFilter extends AbstractStringBasedFilter {

  private static final String NAME = "Regex filter";
  private static final String DESCRIPTION = "Filtering events based on a regular expression.";
  private static final String LABEL_TEXT = "Enter a regular expression";
  private static final String DOT_ALL_SUFFIX = ".*";
  private String condition = "";
  private Pattern pattern = Pattern.compile("");
  private boolean patternOk = true;

  public RegexFilter() {
    super(NAME, DESCRIPTION, LABEL_TEXT);

  }

  protected void performPreFiltering() {
    condition = getFilteringText();
    while (condition.startsWith(DOT_ALL_SUFFIX)) {
      condition = condition.substring(2);
    }
    while (condition.endsWith(DOT_ALL_SUFFIX)) {
      condition = condition.substring(0, condition.length() - 2);
    }
    patternOk = false;
    int flags = 0;
    if (isIgnoreCase()) {
      flags = Pattern.CASE_INSENSITIVE;
    }
    try {
      pattern = Pattern.compile(condition, flags);
      patternOk = true;
    } catch (PatternSyntaxException e) {
      textField.setBackground(Color.RED);
    }

  }

  @Override
  public boolean accept(LogData logData, int row) {
    if (patternOk && condition.length() > 0) {
      return pattern.matcher(logData.getMessage()).find();
    }
    return condition.length() == 0 || !patternOk;
  }

}
