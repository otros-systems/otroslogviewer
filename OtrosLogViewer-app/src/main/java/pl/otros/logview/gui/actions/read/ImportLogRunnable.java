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
package pl.otros.logview.gui.actions.read;

import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.LogImportStats;
import pl.otros.logview.gui.LogViewPanelWrapper;
import pl.otros.logview.importer.LogImporter;
import pl.otros.logview.io.LoadingInfo;
import pl.otros.logview.io.Utils;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.store.LogDataStore;

import javax.swing.*;
import java.util.logging.Logger;

public final class ImportLogRunnable implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(ImportLogRunnable.class.getName());
  private final LoadingInfo openFileObject;
  private final LogViewPanelWrapper panel;
  private final FileObject file;
  private LogImporter importer;

  public ImportLogRunnable(LoadingInfo openFileObject, LogViewPanelWrapper panel, FileObject file, LogImporter importer) {
    this.openFileObject = openFileObject;
    this.panel = panel;
    this.file = file;
    this.importer = importer;
  }

  @Override
  public void run() {
    ParsingContext parsingContext = new ParsingContext(file.getName().getFriendlyURI(), file.getName().getBaseName());
    final LogDataTableModel dataTableModel = panel.getDataTableModel();
    final LogDataStore logDataStore = dataTableModel.getLogDataStore();
    LogImportStats importStats = new LogImportStats(file.getName().getFriendlyURI());
    panel.getStatsTable().setModel(importStats);
    ProgressWatcher watcher = new ProgressWatcher(openFileObject.getObserableInputStreamImpl(), panel, file, importStats);
    Thread t = new Thread(watcher, "Log loader: " + file.getName().toString());
    t.setDaemon(true);
    t.start();
    panel.addHierarchyListener(new ReadingStopperForRemove(openFileObject.getObserableInputStreamImpl()));
    importer.initParsingContext(parsingContext);
    try {
      importer.importLogs(openFileObject.getContentInputStream(), logDataStore, parsingContext);
    } catch (Exception e) {
      LOGGER.severe("Error when importing log: " + e.getMessage());
    }
    LOGGER.info("File " + file.getName().getFriendlyURI() + " loaded");
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        dataTableModel.fireTableDataChanged();
        panel.switchToContentView();
      }
    });
    watcher.updateFinish("Loaded");
    Utils.closeQuietly(openFileObject.getFileObject());
  }
}
