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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.BufferingLogDataCollectorProxy;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.Stoppable;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.parser.TableColumnNameSelfDescribable;
import pl.otros.logview.gui.LogImportStats;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.SelectionMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class TailLogActionListener extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(TailLogActionListener.class.getName());
  private LogImporter importer;

  public TailLogActionListener(OtrosApplication otrosApplication, LogImporter importer) {
    super(otrosApplication);
    this.importer = importer;
  }

  public void actionPerformed(ActionEvent e) {
    JOtrosVfsBrowserDialog chooser = getOtrosApplication().getOtrosVfsBrowserDialog();
    initFileChooser(chooser);

    JOtrosVfsBrowserDialog.ReturnValue result = chooser.showOpenDialog((Component) e.getSource(), "Tail " + importer.getName() + " log");
    if (result != JOtrosVfsBrowserDialog.ReturnValue.Approve) {
      return;
    }
    final FileObject[] files = chooser.getSelectedFiles();
    for (FileObject file : files) {
      openFileObjectInTailMode(file);
    }
  }

  public void openFileObjectInTailMode(final FileObject file) {
    final LoadingInfo loadingInfo;
    try {
      loadingInfo = Utils.openFileObject(file, true);
    } catch (Exception e2) {
      LOGGER.error("Cannot open tailing input stream for " + file.getName().getFriendlyURI() + ", " + e2.getMessage());
      JOptionPane.showMessageDialog(null, "Cannot open tailing input stream for: " + file.getName().getFriendlyURI() + ", " + e2.getMessage(), "Error",
        JOptionPane.ERROR_MESSAGE);
      return;
    }

    TableColumns[] tableColumnsToUse = determineTableColumnsToUse(loadingInfo, importer);

    final LogViewPanelWrapper panel = new LogViewPanelWrapper(file.getName().getBaseName(), loadingInfo.getObserableInputStreamImpl(),
      tableColumnsToUse, getOtrosApplication());
    panel.goToLiveMode();

    String tabName = Utils.getFileObjectShortName(file);
    getOtrosApplication().addClosableTab(tabName, loadingInfo.getFriendlyUrl(), Icons.ARROW_REPEAT, panel, true);

    BufferingLogDataCollectorProxy bufferingLogDataCollectorProxy = new BufferingLogDataCollectorProxy(panel.getDataTableModel(), 2000,
      panel.getConfiguration());
    openFileObjectInTailMode(panel, loadingInfo, bufferingLogDataCollectorProxy);
    SwingUtilities.invokeLater(panel::switchToContentView);
  }

  protected TableColumns[] determineTableColumnsToUse(LoadingInfo loadingInfo, LogImporter importer) {
    TableColumns[] tableColumnsToUse = TableColumns.ALL_WITHOUT_LOG_SOURCE;
    if (importer instanceof TableColumnNameSelfDescribable) {
      TableColumnNameSelfDescribable describable = (TableColumnNameSelfDescribable) importer;
      tableColumnsToUse = describable.getTableColumnsToUse();
    }
    return tableColumnsToUse;
  }

  public void openFileObjectInTailMode(final LogViewPanelWrapper panel, final LoadingInfo loadingInfo, final BufferingLogDataCollectorProxy logDataCollector) {
    ParsingContext parsingContext = new ParsingContext(loadingInfo.getFileObject().getName().getFriendlyURI(), loadingInfo.getFileObject().getName()
      .getBaseName());
    openFileObjectInTailMode(panel, loadingInfo, logDataCollector, parsingContext);

  }

  public void openFileObjectInTailMode(final LogViewPanelWrapper panel, final LoadingInfo loadingInfo, final BufferingLogDataCollectorProxy logDataCollector,
                                       final ParsingContext parsingContext) {
    {

      Runnable r = () -> {
        LogImportStats importStats = new LogImportStats(loadingInfo.getFileObject().getName().getFriendlyURI());
        panel.getStatsTable().setModel(importStats);
        panel.addHierarchyListener(new ReadingStopperForRemove(loadingInfo.getObserableInputStreamImpl(), logDataCollector,
          new ParsingContextStopperForClosingTab(parsingContext)));
        importer.initParsingContext(parsingContext);
        try {
          loadingInfo.setLastFileSize(loadingInfo.getFileObject().getContent().getSize());
        } catch (FileSystemException e1) {
          LOGGER.warn("Can't initialize start position for tailing. Can duplicate some values for small files");
        }
        while (parsingContext.isParsingInProgress()) {
          try {
            importer.importLogs(loadingInfo.getContentInputStream(), logDataCollector, parsingContext);
            if (!loadingInfo.isTailing() || loadingInfo.isGziped()) {
              break;
            }
            Thread.sleep(1000);

            Utils.reloadFileObject(loadingInfo);
          } catch (Exception e) {
            LOGGER.warn("Exception in tailing loop: " + e.getMessage());
          }
        }
        LOGGER.info(String.format("Loading of files %s is finished", loadingInfo.getFriendlyUrl()));
        parsingContext.setParsingInProgress(false);
        LOGGER.info("File " + loadingInfo.getFriendlyUrl() + " loaded");
        getOtrosApplication().getStatusObserver().updateStatus("File " + loadingInfo.getFriendlyUrl() + " stop tailing");
        Utils.closeQuietly(loadingInfo.getFileObject());
      };
      Thread t = new Thread(r, "Log reader-" + loadingInfo.getFileObject().getName().getFriendlyURI());
      t.setDaemon(true);
      t.start();

    }
  }

  private void initFileChooser(JOtrosVfsBrowserDialog chooser) {
    chooser.setSelectionMode(SelectionMode.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
  }

  public LogImporter getImporter() {
    return importer;
  }

  public void setImporter(LogImporter importer) {
    this.importer = importer;
  }

  public static class ReadingStopperForRemove implements HierarchyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadingStopperForRemove.class.getName());

    private final List<SoftReference<Stoppable>> referencesList;

    public ReadingStopperForRemove(Stoppable... stoppables) {
      super();
      referencesList = new ArrayList<>();
      for (Stoppable stoppable : stoppables) {
        referencesList.add(new SoftReference<>(stoppable));
      }
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
      if (e.getChangeFlags() == 1 && e.getChanged().getParent() == null) {
        // if (e.getChangeFlags() == 6) {
        for (SoftReference<Stoppable> ref : referencesList) {
          Stoppable stoppable = ref.get();
          LOGGER.debug("Tab removed, stopping thread if reference is != null (actual: " + stoppable + ")");
          if (stoppable != null) {
            stoppable.stop();
          }
        }
      }

    }
  }

  public static class ParsingContextStopperForClosingTab implements Stoppable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingContextStopperForClosingTab.class.getName());
    private final ParsingContext context;

    public ParsingContextStopperForClosingTab(ParsingContext context) {
      super();
      this.context = context;
    }

    @Override
    public void stop() {
      LOGGER.info("Closing tab, setting parsingInProgress to false.");
      context.setParsingInProgress(false);
    }

  }

}
