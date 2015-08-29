/*
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
 */

package pl.otros.logview.accept;

import pl.otros.logview.LogData;
import pl.otros.logview.filter.LogFilter;

import java.util.Collection;

public class FilteredAcceptCondition extends  AbstractAcceptContidion{
  private final Collection<LogFilter> filtersList;

  public FilteredAcceptCondition(Collection<LogFilter> filtersList) {
    this.filtersList = filtersList;
    name = "Filtered out log events";
    description = "Log events which are filtered out";
  }

  @Override
  public boolean accept(LogData data) {
    for (LogFilter logFilter : filtersList) {
      if (logFilter.isEnable() && !logFilter.accept(data, 0)){
           return true;
      }
    }
    return false;
  }
}
