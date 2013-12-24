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

import pl.otros.logview.LogData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatcher implements SearchMatcher {

  private static final String DOT_ALL_SUFFIX = ".*";
  private Pattern pattern;

  public RegexMatcher(String regex) {
    super();
    while (regex.startsWith(DOT_ALL_SUFFIX)) {
      regex = regex.substring(2);
    }
    while (regex.endsWith(DOT_ALL_SUFFIX)) {
      regex = regex.substring(0, regex.length() - 2);
    }
    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public boolean matches(LogData logData) {
    return pattern.matcher(logData.getMessage()).find();
  }

  @Override
  public ArrayList<String> getFoundTextFragments(LogData logData) {
    HashSet<String> result = new HashSet<String>();
    Matcher matcher = pattern.matcher(logData.getMessage());
    while (matcher.find()) {
      result.add(matcher.group());
    }
    return new ArrayList<String>(result);
  }
}
