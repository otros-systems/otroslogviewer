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

import pl.otros.logview.api.AutomaticMarker;
import pl.otros.logview.api.LogData;

import java.util.Properties;

public class StringMarker extends PropertyFileAbstractMarker implements AutomaticMarker {

  public StringMarker(Properties p) throws Exception {
    super(p);
  }

  @Override
  public boolean toMark(LogData data) {
    String message = ignoreCase ? data.getMessage().toLowerCase() : data.getMessage();
    return include && message.contains(condition) || !include && !message.contains(condition);

  }

  @Override
  public Properties toProperties() {

    Properties properties = super.toProperties();
    properties.put(TYPE, TYPE_STRING);
    return properties;
  }

}
