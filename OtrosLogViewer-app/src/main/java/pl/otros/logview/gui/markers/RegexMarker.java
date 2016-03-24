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
package pl.otros.logview.gui.markers;

import pl.otros.logview.api.LogData;

import java.util.Properties;
import java.util.regex.Pattern;

public class RegexMarker extends PropertyFileAbstractMarker {

  private final String precondition;
  private final Pattern regexCondition;

  private final boolean preconditionInclude = true;

  public RegexMarker(Properties p) throws Exception {
    super(p);
    if (!p.containsKey(PRECONDITION) || !p.containsKey(CONDITION) || !p.containsKey(PRECONDITION_INCLUDE)) {
      throw new Exception("Not enought parameters");
    }

    if (ignoreCase) {
      this.regexCondition = Pattern.compile(p.getProperty(CONDITION), Pattern.CASE_INSENSITIVE);
      this.precondition = p.getProperty(PRECONDITION).toLowerCase();
    } else {
      this.precondition = p.getProperty(PRECONDITION);
      this.regexCondition = Pattern.compile(p.getProperty(CONDITION));
    }

  }

  @Override
  public Properties toProperties() {

    Properties p = super.toProperties();
    p.put(PRECONDITION, precondition);
    p.put(PRECONDITION_INCLUDE, Boolean.toString(preconditionInclude));
    p.put(TYPE, TYPE_REGEX);
    return p;
  }

  @Override
  public boolean toMark(LogData data) {
    String message = ignoreCase ? data.getMessage().toLowerCase() : data.getMessage();

    // Checking precondition for performance reason
    if (preconditionInclude && !message.contains(precondition)) {
      return false;
    }
    boolean result = regexCondition.matcher(message).find();
    return result;
  }

}
