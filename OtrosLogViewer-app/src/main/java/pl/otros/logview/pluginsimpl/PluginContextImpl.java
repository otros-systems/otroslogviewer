/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.pluginsimpl;

import pl.otros.logview.api.plugins.LogOpenHandler;
import pl.otros.logview.api.plugins.MenuActionProvider;
import pl.otros.logview.api.plugins.PluginContext;
import pl.otros.logview.gui.OtrosApplication;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginContextImpl implements PluginContext {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginContextImpl.class.getName());

  private OtrosApplication application;
  private LogOpenHandler logOpenHandler;


  public PluginContextImpl(OtrosApplication application) {
    super();
    this.application = application;
    logOpenHandler = new LogOpenHandlerImpl(this);
  }


  @Override
  public OtrosApplication getOtrosApplication() {
    return application;
  }

  @Override
  public void addClosableTab(String name, String tooltip, Icon icon, JComponent component, boolean show) {
    application.addClosableTab(name, tooltip, icon, component, show);
  }

  @Override
  public LogOpenHandler getLogOpenHandler() {
    return logOpenHandler;
  }


  @Override
  public void addLogViewPanelMenuActionProvider(MenuActionProvider menuActionProvider) {
    application.addLogViewPanelMenuActionProvider(menuActionProvider);
  }


}
