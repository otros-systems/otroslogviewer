/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.filter;

import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.pluginable.LogFilter;
import pl.otros.logview.api.pluginable.LogFilterValueChangeListener;

import java.util.Properties;

public abstract class AbstractLogFilter implements LogFilter {

  protected boolean enable;
  protected LogDataTableModel collector;
  protected LogFilterValueChangeListener listener;
  protected String name;
  protected String description;

  public AbstractLogFilter(String name, String description) {
    this.name = name;
    this.description = description;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void init(Properties properties, LogDataTableModel logDataTableModel) {

  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getPluginableId() {
    return this.getClass().getName();
  }

  @Override
  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  @Override
  public boolean isEnable() {
    return enable;
  }

  @Override
  public void setValueChangeListener(LogFilterValueChangeListener listener) {
    this.listener = listener;
  }

  @Override
  public int getApiVersion() {
    return LOG_FILTER_VERSION_1;
  }

}
