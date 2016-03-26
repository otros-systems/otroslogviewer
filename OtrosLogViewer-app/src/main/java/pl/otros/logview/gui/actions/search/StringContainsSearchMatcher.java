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

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.model.LogData;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Matches if LogData message contains string (case insensitive)
 * 
 * 
 */
public class StringContainsSearchMatcher implements SearchMatcher {

  private final String searchChar;

  public StringContainsSearchMatcher(String searchChar) {
    this.searchChar = searchChar.toLowerCase();
  }

  @Override
  public boolean matches(LogData logData) {
    return StringUtils.contains(StringUtils.lowerCase(logData.getMessage()), searchChar);
  }

  @Override
  public ArrayList<String> getFoundTextFragments(LogData logData) {
    HashSet<String> result = new HashSet<>();
    String message = logData.getMessage();
    String messageLc = message.toLowerCase();
    int idx = 0;
    while ((idx = messageLc.indexOf(searchChar, idx)) > -1) {
      result.add(message.substring(idx, idx + searchChar.length()));
      idx++;
    }

    return new ArrayList<>(result);
  }

}
