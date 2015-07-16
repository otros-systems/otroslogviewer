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
package pl.otros.logview.loader;

import pl.otros.logview.api.plugins.Plugin;
import pl.otros.logview.filter.LogFilter;
import pl.otros.logview.gui.StatusObserver;
import pl.otros.logview.gui.markers.AutomaticMarker;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.SoapMessageFormatter;
import pl.otros.logview.gui.message.json.JsonMessageFormatter;
import pl.otros.logview.gui.message.stacktracecode.StackTraceFormatterPlugin;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.importer.LogImporter;
import pl.otros.logview.pluginable.AllPluginables;
import pl.otros.logview.pluginable.PluginableElement;
import pl.otros.logview.pluginable.PluginableElementsContainer;
import pl.otros.logview.pluginable.PluginablePluginAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LvDynamicLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(LvDynamicLoader.class.getName());
  private static LvDynamicLoader instance = null;
  private LogImportersLoader logImportersLoader;
  private LogFiltersLoader logFiltersLoader;
  private MessageColorizerLoader messageColorizerLoader;
  private StatusObserver statusObserver;

  public static LvDynamicLoader getInstance() {
    if (instance == null) {
      synchronized (LvDynamicLoader.class) {
        if (instance == null) {
          instance = new LvDynamicLoader();

        }
      }
    }
    return instance;
  }

  private Collection<AutomaticMarker> automaticMarkers;
  private Collection<LogImporter> logImporters;
  private Collection<LogFilter> logFilters;
  private Collection<MessageColorizer> messageColorizers;
  private Collection<MessageFormatter> messageFormatters;
  private Collection<Plugin> pluginInfos; 
  private BaseLoader baseLoader;

  private LvDynamicLoader() {
    super();
    baseLoader = new BaseLoader();
    logImportersLoader = new LogImportersLoader();
    logFiltersLoader = new LogFiltersLoader();
    messageColorizerLoader = new MessageColorizerLoader();

    automaticMarkers = new ArrayList<AutomaticMarker>();
    logImporters = new ArrayList<LogImporter>();
    logFilters = new ArrayList<LogFilter>();
    messageColorizers = new ArrayList<MessageColorizer>();
    messageFormatters = new ArrayList<MessageFormatter>();
    pluginInfos = new ArrayList<Plugin>();
  }

  public void loadAll() throws IOException, InitializationException {
    updateStatus("Loading automatic markers");
    loadAutomaticMarkers();

    updateStatus("Loading log filters");
    loadLogFilters();

    updateStatus("Loading log importers");
    InitializationException nonfatalIE = null;
    try {
        loadLogImporters();
    } catch (InitializationException ie) {
        // This attempts to complete initialization in degraded state, but
        // still notify user of the problem.
        nonfatalIE = ie;
    }

    updateStatus("Loading message colorizers");
    loadMessageColorized();

    updateStatus("Loading message formatters");
    loadMessageFormatter();
    
    updateStatus("Loading plugins");
    loadPlugins();
    if (nonfatalIE != null) throw nonfatalIE;
  }

private void loadPlugins() {
	pluginInfos.add(new StackTraceFormatterPlugin());
	pluginInfos.addAll(baseLoader.load(AllPluginables.USER_PLUGINS, Plugin.class));
    pluginInfos.addAll(baseLoader.load(AllPluginables.SYSTEM_PLUGINS, Plugin.class));
    ArrayList<PluginablePluginAdapter> pluList = new ArrayList<PluginablePluginAdapter>();
    for (Plugin pluginInfo : pluginInfos) {
		pluList.add(new PluginablePluginAdapter(pluginInfo));
	}
    addElementsToList(AllPluginables.getInstance().getPluginsInfoContainer(), pluList, 0);
}

  private void loadMessageFormatter() {
    messageFormatters.add(new SoapMessageFormatter());
    messageFormatters.add(new JsonMessageFormatter());
    messageFormatters.addAll(baseLoader.load(new File("./plugins/message"), MessageFormatter.class));
    messageFormatters.addAll(baseLoader.load(AllPluginables.USER_MESSAGE_FORMATTER_COLORZIERS, MessageFormatter.class));
    PluginableElementsContainer<MessageFormatter> pluginableElementsContainer = AllPluginables.getInstance().getMessageFormatters();
    addElementsToList(pluginableElementsContainer, messageFormatters, MessageFormatter.MESSAGE_FORMATTER_VERSION_1);
  }

  private void loadMessageColorized() {
    File file = new File("./plugins/message/");
    messageColorizers.addAll(messageColorizerLoader.loadInternal());
    messageColorizers.addAll(messageColorizerLoader.loadFromJars(file));
    messageColorizers.addAll(messageColorizerLoader.loadFromProperies(file));
    messageColorizers.addAll(messageColorizerLoader.loadFromJars(AllPluginables.USER_MESSAGE_FORMATTER_COLORZIERS));
    messageColorizers.addAll(messageColorizerLoader.loadFromProperies(AllPluginables.USER_MESSAGE_FORMATTER_COLORZIERS));
    PluginableElementsContainer<MessageColorizer> pluginableElementsContainer = AllPluginables.getInstance().getMessageColorizers();
    addElementsToList(pluginableElementsContainer, messageColorizers, MessageColorizer.MESSAGE_COLORIZER_VERSION_CURRENT);

  }

  private void loadAutomaticMarkers() throws IOException {

    automaticMarkers.addAll(AutomaticMarkerLoader.loadInternalMarkers());
    File f = new File("plugins/markers");
    automaticMarkers.addAll(AutomaticMarkerLoader.load(f));
    automaticMarkers.addAll(AutomaticMarkerLoader.loadRegexMarkers(f));
    automaticMarkers.addAll(AutomaticMarkerLoader.loadStringMarkers(f));
    automaticMarkers.addAll(AutomaticMarkerLoader.loadPatternMarker(f));

    automaticMarkers.addAll(AutomaticMarkerLoader.load(AllPluginables.USER_MARKERS));
    automaticMarkers.addAll(AutomaticMarkerLoader.loadRegexMarkers(AllPluginables.USER_MARKERS));
    automaticMarkers.addAll(AutomaticMarkerLoader.loadStringMarkers(AllPluginables.USER_MARKERS));
    automaticMarkers.addAll(AutomaticMarkerLoader.loadPatternMarker(AllPluginables.USER_MARKERS));

    PluginableElementsContainer<AutomaticMarker> markersContainser = AllPluginables.getInstance().getMarkersContainser();
    addElementsToList(markersContainser, automaticMarkers, AutomaticMarker.AUTOMATIC_MARKER_VERSION_1);

  }

  private void loadLogImporters() throws InitializationException {
    loadLogImportersFromUserHome();
    InitializationException deferredIE = null;
    try {
        loadLogImportersFromPluginDirectory();
    } catch (InitializationException ie) {
        // This is so we can still attempt to load other LogImporters even
        // though some may have failed.
        deferredIE = ie;
    }
    loadLogImportsThatAreProvidedInternally();
    PluginableElementsContainer<LogImporter> logImportersContainer = AllPluginables.getInstance().getLogImportersContainer();
    addElementsToList(logImportersContainer, logImporters, LogImporter.LOG_IMPORTER_VERSION_1);
    if (deferredIE != null) throw deferredIE;
  }

  private void loadLogImportsThatAreProvidedInternally() throws InitializationException {
    logImporters.addAll(logImportersLoader.loadInternalLogImporters());
  }

  private void loadLogImportersFromPluginDirectory()
  throws InitializationException {
    File f = new File("plugins/logimporters");
    logImporters.addAll(logImportersLoader.load(f));
    logImporters.addAll(logImportersLoader.loadPropertyPatternFileFromDir(f));
  }

  private void loadLogImportersFromUserHome() throws InitializationException {
    logImporters.addAll(logImportersLoader.load(AllPluginables.USER_LOG_IMPORTERS));
    logImporters.addAll(logImportersLoader.loadPropertyPatternFileFromDir(AllPluginables.USER_LOG_IMPORTERS));
  }

  private void loadLogFilters() {
    logFilters.addAll(logFiltersLoader.loadInternalFilters());
    logFilters.addAll(baseLoader.load(new File("plugins/filters"), LogFilter.class));
    logFilters.addAll(baseLoader.load(AllPluginables.USER_FILTER, LogFilter.class));
    PluginableElementsContainer<LogFilter> logFiltersContainer = AllPluginables.getInstance().getLogFiltersContainer();
    addElementsToList(logFiltersContainer, logFilters, LogFilter.LOG_FILTER_VERSION_1);

  }

  protected <T extends PluginableElement> void addElementsToList(PluginableElementsContainer<T> container, Collection<T> list, int... versions) {
    for (T element : list) {
      // if pluginable element is wrong e.g. do not have method
      Set<Integer> versionsSet = new HashSet<Integer>();
      for (int i : versions) {
        versionsSet.add(i);
      }
      try {
        if (versionsSet.contains(element.getApiVersion())) {
          container.addElement(element);
        } else {
          LOGGER.warn("Cant add pluginable element " + element.getPluginableId() + ", wrong version: " + element.getApiVersion());
        }
      } catch (Throwable e) {
        LOGGER.warn("Cant add pluginable element " + element);
      }
    }
  }

  private void updateStatus(String status) {
    if (statusObserver != null) {
      statusObserver.updateStatus(status);
    }
  }

  public StatusObserver getStatusObserver() {
    return statusObserver;
  }

  public void setStatusObserver(StatusObserver statusObserver) {
    this.statusObserver = statusObserver;
  }

}
