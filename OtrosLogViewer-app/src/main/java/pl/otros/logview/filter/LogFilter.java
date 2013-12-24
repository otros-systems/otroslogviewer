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

import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.pluginable.PluginableElement;

import java.awt.*;
import java.util.Properties;

public interface LogFilter extends PluginableElement {

  public static int LOG_FILTER_VERSION_1 = 1;

  public String getName();

  public void init(Properties properties, LogDataTableModel logDataTableModel);

  public boolean accept(LogData logData, int row);

  public void setEnable(boolean enable);

  public boolean isEnable();

  public Component getGUI();

  public void setValueChangeListener(LogFilterValueChangeListener listener);

}
