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

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.BufferingLogDataCollectorProxy;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.plugins.PluginContext;
import pl.otros.logview.gui.actions.TailLogActionListener;
import pl.otros.logview.gui.actions.read.ReadingStopperForRemove;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;

import javax.swing.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 */
class OpenLogsSwingWorker extends SwingWorker<Void, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenLogsSwingWorker.class.getName());

  private LogViewPanelWrapper logViewPanelWrapper;
  private final PluginContext pluginContext;
  private final FileObject[] fileObjects;
  private final String tabName;

  public OpenLogsSwingWorker(PluginContext pluginContext, String tabName, FileObject[] fileObjects) {
    this.pluginContext = pluginContext;
    this.fileObjects = fileObjects;
    this.tabName = tabName;
  }

  @Override
  protected Void doInBackground() throws Exception {
    LoadingInfo[] loadingInfos = getLoadingInfo();

    Collection<LogImporter> elements = pluginContext.getOtrosApplication().getAllPluginables().getLogImportersContainer().getElements();
    LogImporter[] importers = elements.toArray(new LogImporter[elements.size()]);
    String[] names = new String[elements.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = importers[i].getName();
    }

    TableColumns[] visibleColumns = {TableColumns.ID,//
      TableColumns.TIME,//
      TableColumns.LEVEL,//
      TableColumns.MESSAGE,//
      TableColumns.CLASS,//
      TableColumns.METHOD,//
      TableColumns.THREAD,//
      TableColumns.MARK,//
      TableColumns.NOTE,//
      TableColumns.LOG_SOURCE

    };

    BaseConfiguration configuration = new BaseConfiguration();
    configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
    configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);
    logViewPanelWrapper = new LogViewPanelWrapper(tabName, null, visibleColumns, pluginContext.getOtrosApplication());
    BufferingLogDataCollectorProxy logDataCollector = new BufferingLogDataCollectorProxy(logViewPanelWrapper.getDataTableModel(), 4000, configuration);

    LogImporter importer = getLogImporter(elements);

    for (LoadingInfo loadingInfo : loadingInfos) {
      openLog(logDataCollector, importer, loadingInfo);
    }
    publish("All log files loaded");

    return null;

  }

  private void openLog(final BufferingLogDataCollectorProxy logDataCollector, final LogImporter importer, final LoadingInfo loadingInfo) {
    publish("Start tailing " + loadingInfo.getFriendlyUrl());

    Runnable r = () -> {
      ParsingContext parsingContext = new ParsingContext(loadingInfo.getFriendlyUrl(), loadingInfo.getFileObject().getName()
        .getBaseName());
      logViewPanelWrapper.addHierarchyListener(new ReadingStopperForRemove(loadingInfo.getObservableInputStreamImpl()));
      logViewPanelWrapper.addHierarchyListener(new ReadingStopperForRemove(logDataCollector));
      logViewPanelWrapper.addHierarchyListener(new ReadingStopperForRemove(new TailLogActionListener.ParsingContextStopperForClosingTab(parsingContext)));
      importer.initParsingContext(parsingContext);
      try {
        loadingInfo.resetLastFileSize();
      } catch (IOException e1) {
        LOGGER.warn("Can't initialize start position for tailing. Can duplicate some values for small files");
      }
      while (parsingContext.isParsingInProgress()) {
        try {
          importer.importLogs(loadingInfo.getContentInputStream(), logDataCollector, parsingContext);
          if (!loadingInfo.isTailing() || loadingInfo.isGzipped()) {
            break;
          }
          Thread.sleep(1000);

          loadingInfo.reloadIfFileSizeChanged();
        } catch (Exception e) {
          LOGGER.warn("Exception in tailing loop: " + e.getMessage());
        }
      }
      LOGGER.info(String.format("Loading of files %s is finished", loadingInfo.getFriendlyUrl()));
      parsingContext.setParsingInProgress(false);
      LOGGER.info("File " + loadingInfo.getFriendlyUrl() + " loaded");
      pluginContext.getOtrosApplication().getStatusObserver().updateStatus("File " + loadingInfo.getFriendlyUrl() + " stop tailing");
      loadingInfo.close();
    };
    Thread t = new Thread(r, "Log reader-" + loadingInfo.getFileObject().getName().getFriendlyURI());
    t.setDaemon(true);
    t.start();


    String friendlyURI = loadingInfo.getFileObject().getName().getFriendlyURI();
    String baseName = loadingInfo.getFileObject().getName().getBaseName();
    ParsingContext parsingContext = new ParsingContext(friendlyURI, baseName);
    logViewPanelWrapper.addHierarchyListener(
      new TailLogActionListener.ReadingStopperForRemove(loadingInfo.getObservableInputStreamImpl(),
        logDataCollector,
        new TailLogActionListener.ParsingContextStopperForClosingTab(parsingContext)));
  }

  private LogImporter getLogImporter(Collection<LogImporter> elements) {
    LogImporter importer = new DetectOnTheFlyLogImporter(elements);
    try {
      importer.init(new Properties());
    } catch (InitializationException e1) {
      LOGGER.error("Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage());
      JOptionPane.showMessageDialog(null, "Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage(), "Open error",
        JOptionPane.ERROR_MESSAGE);
    }
    return importer;
  }

  private LoadingInfo[] getLoadingInfo() {
    ArrayList<LoadingInfo> list = new ArrayList<>();
    publish("Opening  log files");
    for (final FileObject file : fileObjects) {
      try {
        list.add(new LoadingInfo(file, true));
      } catch (Exception e1) {
        String msg = String.format("Can't open file %s: %s", file.getName().getFriendlyURI(), e1.getMessage());
        publish(msg);
        LOGGER.warn(msg);
      }
    }
    LoadingInfo[] loadingInfos = new LoadingInfo[list.size()];
    loadingInfos = list.toArray(loadingInfos);
    return loadingInfos;
  }

  @Override
  protected void done() {
    logViewPanelWrapper.goToLiveMode();
    logViewPanelWrapper.switchToContentView();
    pluginContext.addClosableTab(tabName, getTooltip(), Icons.ARROW_REPEAT, logViewPanelWrapper, true);

  }

  private String getTooltip() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>Multiple files:<br>");
    for (FileObject fo : fileObjects) {
      sb.append(fo.getName().getFriendlyURI());
      sb.append("<BR>");
    }
    sb.append("</html>");
    return sb.toString();
  }

  @Override
  protected void process(List<String> chunks) {
    if (chunks.size() > 0) {
      pluginContext.getOtrosApplication().getStatusObserver().updateStatus(chunks.get(0));
    }
  }
}
