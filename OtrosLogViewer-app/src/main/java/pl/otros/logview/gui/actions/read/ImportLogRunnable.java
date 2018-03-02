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
package pl.otros.logview.gui.actions.read;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.parser.ParsingContext;

import javax.swing.*;

public final class ImportLogRunnable implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImportLogRunnable.class.getName());
  private final LoadingInfo openFileObject;
  private final LogViewPanelWrapper panel;
  private final FileObject file;
  private final LogImporter importer;

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
    panel.addHierarchyListener(new ReadingStopperForRemove(openFileObject.getObserableInputStreamImpl()));
    importer.initParsingContext(parsingContext);
    try {
      importer.importLogs(openFileObject.getContentInputStream(), logDataStore, parsingContext);
      LOGGER.info("File " + file.getName().getFriendlyURI() + " loaded");
    } catch (Exception e) {
      LOGGER.error("Error when importing log", e);
    }
    SwingUtilities.invokeLater(() -> {
      dataTableModel.fireTableDataChanged();
      panel.switchToContentView();
    });
    openFileObject.close();
  }
}
