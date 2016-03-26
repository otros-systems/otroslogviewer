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

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.model.LogData;

import java.util.Collection;

public class PropertyFilter extends AbstractStringBasedFilter {

  private static final String NAME = "Property filter";
  private static final String DESCRIPTION = "Filtering based on property";
  private static final String LABEL_TEXT = "Enter a string, PROP=VALUE or PROP~=VALUE";
  private String condition = "";
  private String property;
  private String value;
  protected Mode mode = Mode.AllProperties;

  protected enum Mode {
    SinglePropertyEquals, SinglePropertyLike, AllProperties
  }

  public PropertyFilter() {
    super(NAME, DESCRIPTION, LABEL_TEXT);

  }

  @Override
  public boolean accept(LogData logData, int row) {
    if (mode.equals(Mode.AllProperties) && StringUtils.isBlank(condition)) {
      return true;
    }
    if (mode.equals(Mode.AllProperties)) {
      Collection<String> values = logData.getProperties().values();
      for (String string : values) {
        if (contains(string, condition, isIgnoreCase())) {
          return true;
        }
      }
    } else if (mode.equals(Mode.SinglePropertyEquals)) {
      String propValue = logData.getProperties().get(property);
      if (isIgnoreCase()) {
        return StringUtils.equalsIgnoreCase(propValue, value);
      } else {
        return StringUtils.equals(propValue, value);
      }
    } else if (mode.equals(Mode.SinglePropertyLike)) {
      String propValue = logData.getProperties().get(property);
      return contains(propValue, value, ignoreCase);
    }
    return false;
  }

  @Override
  protected void performPreFiltering() {
    String text = getFilteringText();
    if (StringUtils.contains(text, "=")) {
      if (StringUtils.contains(text, "~=")) {
        mode = Mode.SinglePropertyLike;
        property = StringUtils.split(text, "~=", 2)[0];
        value = StringUtils.split(text, "~=", 2)[1];
      } else {
        mode = Mode.SinglePropertyEquals;
        property = StringUtils.split(text, "=", 2)[0];
        value = StringUtils.split(text, "=", 2)[1];
      }
    } else {
      mode = Mode.AllProperties;
      condition = text;
    }
  }

  protected boolean contains(String string, String searchStr, boolean ignoreCase) {
    if (ignoreCase) {
      return StringUtils.containsIgnoreCase(string, searchStr);
    } else {
      return StringUtils.contains(string, searchStr);
    }
  }

  public void setCondition(String key, String value){
      textField.setText(key+"="+value);
  }
}
