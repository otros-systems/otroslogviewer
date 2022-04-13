/******************************************************************************
 Copyright 2011 Krzysztof Otrebski
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package pl.otros.logview.api.pluginable;

import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.theme.Theme;

import java.awt.*;
import java.util.Properties;

public interface LogFilter extends PluginableElement {

  int LOG_FILTER_VERSION_1 = 1;

  String getName();

  default void init(Properties properties, LogDataTableModel logDataTableModel, Theme theme) {
  }

  boolean accept(LogData logData, int row);

  void setEnable(boolean enable);

  boolean isEnable();

  Component getGUI();

  void setValueChangeListener(LogFilterValueChangeListener listener);

}
