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
package pl.otros.logview.api.pluginable;

import pl.otros.logview.api.importer.LogImporter;

import java.io.File;

public class AllPluginables {

  private final PluginableElementsContainer<LogImporter> logImportersContainer;
  private final PluginableElementsContainer<AutomaticMarker> markersContainer;
  private final PluginableElementsContainer<MessageColorizer> messageColorizers;
  private final PluginableElementsContainer<MessageFormatter> messageFormatters;
  private PluginableElementsContainer<LogFilter> logFiltersContainer;
  private final PluginableElementsContainer<PluginablePluginAdapter> pluginsInfoContainer;
  private final PluginableElementsContainer<PluginableElement> allPluginables;

  public static final File USER_CONFIGURATION_DIRECTORY = new File(System.getProperty("user.home") + File.separator + ".otroslogviewer");
  public static final File USER_MARKERS = new File(USER_CONFIGURATION_DIRECTORY, "plugins" + File.separator + "markers");
  public static final File USER_FILTER = new File(USER_CONFIGURATION_DIRECTORY, "plugins" + File.separator + "filters");
  public static final File USER_LOG_IMPORTERS = new File(USER_CONFIGURATION_DIRECTORY, "plugins" + File.separator + "logimporters");
  public static final File USER_MESSAGE_FORMATTER_COLORZIERS = new File(USER_CONFIGURATION_DIRECTORY, "plugins" + File.separator + "message");
  public static final File USER_PLUGINS = new File(USER_CONFIGURATION_DIRECTORY, "plugins" + File.separator + "plugins");
  public static final File SYSTEM_PLUGINS = new File("plugins" + File.separator + "plugins");

  private static final AllPluginables instance = new AllPluginables();

  public static AllPluginables getInstance() {
    return instance;
  }

  public AllPluginables() {
    markersContainer = new PluginableElementsContainer<>();
    messageColorizers = new PluginableElementsContainer<>();
    messageFormatters = new PluginableElementsContainer<>();
    logFiltersContainer = new PluginableElementsContainer<>();
    logFiltersContainer = new PluginableElementsContainer<>();
    logImportersContainer = new PluginableElementsContainer<>();
    allPluginables = new PluginableElementsContainer<>();
    pluginsInfoContainer = new PluginableElementsContainer<>();
  }

  public PluginableElementsContainer<LogImporter> getLogImportersContainer() {
    return logImportersContainer;
  }

  public PluginableElementsContainer<LogFilter> getLogFiltersContainer() {
    return logFiltersContainer;
  }

  public PluginableElementsContainer<AutomaticMarker> getMarkersContainser() {
    return markersContainer;
  }

  public PluginableElementsContainer<PluginableElement> getAllPluginables() {
    return allPluginables;
  }

  public PluginableElementsContainer<MessageColorizer> getMessageColorizers() {
    return messageColorizers;
  }

  public PluginableElementsContainer<MessageFormatter> getMessageFormatters() {
    return messageFormatters;
  }

  public PluginableElementsContainer<PluginablePluginAdapter> getPluginsInfoContainer() {
    return pluginsInfoContainer;
  }


}
