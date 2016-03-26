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
package pl.otros.logview.accept;

import pl.otros.logview.api.model.LogData;

import java.util.logging.Level;

public class LowLevelAcceptCondition extends AbstractAcceptContidion {

  private final Level level;

  public LowLevelAcceptCondition() {
    level = Level.INFO;
    name = "Level>=" + level.getName();
    description = "Level lower then " + level.getName();
  }

  @Override
  public boolean accept(LogData data) {
    return data.getLevel().intValue() < level.intValue();
  }

}
