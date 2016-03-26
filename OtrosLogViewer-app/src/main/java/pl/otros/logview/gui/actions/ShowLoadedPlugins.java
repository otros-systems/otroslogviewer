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
package pl.otros.logview.gui.actions;

import pl.otros.logview.api.pluginable.LogFilter;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.pluginable.AutomaticMarker;
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.MessageFormatter;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.api.pluginable.PluginableElement;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowLoadedPlugins extends OtrosAction {


  private final JComponent loadedComponents;
  private final JTextArea textArea;

  public ShowLoadedPlugins(OtrosApplication otrosApplication) {
    super(otrosApplication);
    putValue(NAME, "Show loaded plugins");
    putValue(SHORT_DESCRIPTION, "Show loaded plugins.");
    putValue(SMALL_ICON, Icons.PLUGIN);

    textArea = new JTextArea();
    textArea.setEditable(false);
    JScrollPane jScrollPane = new JScrollPane(textArea);
    loadedComponents = jScrollPane;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    refreshData();
    getOtrosApplication().addClosableTab("Loaded plugins","List loaded plugins",Icons.PLUGIN,loadedComponents,true);

  }

  protected void refreshData() {
    textArea.setText("");
    StringBuilder sb = new StringBuilder();
    AllPluginables pluginables = AllPluginables.getInstance();
    PluginableElementsContainer<LogFilter> logFiltersContainer = pluginables.getLogFiltersContainer();
    PluginableElementsContainer<AutomaticMarker> markersContainser = pluginables.getMarkersContainser();
    PluginableElementsContainer<MessageColorizer> messageColorizers = pluginables.getMessageColorizers();
    PluginableElementsContainer<MessageFormatter> messageFormatters = pluginables.getMessageFormatters();

    sb.append("Log filters:\n");
    for (PluginableElement element : logFiltersContainer.getElements()) {
      sb.append("\t").append(element.getName()).append(" [").append(element.getPluginableId()).append("]\n");
    }

    sb.append("\nMarkers:\n");
    for (PluginableElement element : markersContainser.getElements()) {
      sb.append("\t").append(element.getName()).append(" [").append(element.getPluginableId()).append("]\n");
    }

    sb.append("\nMessage colorizers:\n");
    for (PluginableElement element : messageColorizers.getElements()) {
      sb.append("\t").append(element.getName()).append(" [").append(element.getPluginableId()).append("]\n");
    }

    sb.append("\nMessage formatters:\n");
    for (PluginableElement element : messageFormatters.getElements()) {
      sb.append("\t").append(element.getName()).append(" [").append(element.getPluginableId()).append("]\n");
    }
    textArea.setText(sb.toString());

  }
}
