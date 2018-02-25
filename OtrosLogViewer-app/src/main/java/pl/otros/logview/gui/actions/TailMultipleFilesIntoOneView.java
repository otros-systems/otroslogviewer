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


import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.loading.LogLoader;
import pl.otros.logview.api.loading.LogLoadingSession;
import pl.otros.logview.api.loading.VfsSource;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.SelectionMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.stream.Collectors;


public class TailMultipleFilesIntoOneView extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(TailMultipleFilesIntoOneView.class.getName());


  public TailMultipleFilesIntoOneView(OtrosApplication otrosApplication) {
    super(otrosApplication);
    putValue(Action.NAME, "Tail multiple log files into one view");
    putValue(Action.SHORT_DESCRIPTION, "Tail multiple log files into one view with log format autodetection");
    putValue(Action.SMALL_ICON, Icons.ARROW_JOIN);
  }

  @Override
  protected void actionPerformedHook(ActionEvent e) {

    final FileObject[] files = getFileObjects(e);
    if (files == null || files.length == 0) {
      return;
    }
    openFileObjectsIntoOneView(files, e.getSource());
  }

  public void openFileObjectsIntoOneView(FileObject[] files, Object guiSource) {
    ArrayList<LoadingInfo> list = new ArrayList<>();
    for (final FileObject file : files) {
      try {
        list.add(Utils.openFileObject(file, true));
      } catch (Exception e1) {
        LOGGER.warn(String.format("Can't open file %s: %s", file.getName().getFriendlyURI(), e1.getMessage()));
      }
    }

    if (list.size() == 0) {
      JOptionPane.showMessageDialog((Component) guiSource, "No files can be opened :(", "Open error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    LoadingInfo[] loadingInfos = new LoadingInfo[list.size()];
    loadingInfos = list.toArray(loadingInfos);

    Collection<LogImporter> elements = AllPluginables.getInstance().getLogImportersContainer().getElements();
    LogImporter[] importers = elements.toArray(new LogImporter[elements.size()]);
    String[] names = new String[elements.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = importers[i].getName();
    }

    TableColumns[] visibleColumns = {
      TableColumns.ID,//
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
    final LogViewPanelWrapper logViewPanelWrapper = new LogViewPanelWrapper("Multiple log files " + loadingInfos.length, null, visibleColumns, getOtrosApplication());

    logViewPanelWrapper.goToLiveMode();
    BaseConfiguration configuration = new BaseConfiguration();
    configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
    configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);
    LogDataCollector logDataCollector = logViewPanelWrapper.getDataTableModel();

    StringBuilder sb = new StringBuilder();
    sb.append("<html>Multiple files:<br>");
    for (LoadingInfo loadingInfo : loadingInfos) {
      sb.append(loadingInfo.getFriendlyUrl());
      sb.append("<BR>");
    }
    sb.append("</html>");

    getOtrosApplication().addClosableTab(String.format("Multiple logs [%d]", loadingInfos.length), sb.toString(), Icons.ARROW_REPEAT, logViewPanelWrapper, true);

    LogImporter importer = new DetectOnTheFlyLogImporter(elements);
    try {
      importer.init(new Properties());
    } catch (InitializationException e1) {
      LOGGER.error("Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage());
      JOptionPane.showMessageDialog((Component) guiSource, "Cant initialize DetectOnTheFlyLogImporter: " + e1.getMessage(), "Open error",
        JOptionPane.ERROR_MESSAGE);

    }
    final LogLoader logLoader = getOtrosApplication().getLogLoader();

    final java.util.List<LogLoadingSession> collect = Arrays.asList(loadingInfos)
      .stream()
      .map(loadingInfo -> logLoader.startLoading(new VfsSource(loadingInfo.getFileObject()), importer, logDataCollector, 3000, Optional.of(2000L)))
      .collect(Collectors.toList());

     LOGGER.info("Have sessions: {}" , collect.stream().map(LogLoadingSession::getId).collect(Collectors.joining(", ")));
    logViewPanelWrapper.onClose(() -> logLoader.close(logDataCollector));

    SwingUtilities.invokeLater(logViewPanelWrapper::switchToContentView);
  }

  private FileObject[] getFileObjects(ActionEvent e) {
    JOtrosVfsBrowserDialog chooser = getOtrosApplication().getOtrosVfsBrowserDialog();
    initFileChooser(chooser);
    JOtrosVfsBrowserDialog.ReturnValue result = chooser.showOpenDialog((Component) e.getSource(), "Open multiple log files into one view");
    if (result != JOtrosVfsBrowserDialog.ReturnValue.Approve) {
      return null;
    }
    return chooser.getSelectedFiles();
  }

  private void initFileChooser(JOtrosVfsBrowserDialog chooser) {
    chooser.setSelectionMode(SelectionMode.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
  }

}
