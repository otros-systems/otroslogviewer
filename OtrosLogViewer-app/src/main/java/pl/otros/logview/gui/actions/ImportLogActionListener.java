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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.*;
import pl.otros.logview.gui.*;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.ObservableInputStreamImpl;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.parser.TableColumnNameSelfDescribable;
import pl.otros.logview.api.reader.ProxyLogDataCollector;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.SelectionMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;

public class ImportLogActionListener extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImportLogActionListener.class.getName());
  private LogImporter importer;
  private LogImportStats importStats;

  public ImportLogActionListener(LogImporter importer, OtrosApplication otrosApplication) {
    super(otrosApplication);
    this.importer = importer;
		this.putValue(SMALL_ICON,importer.getIcon());
		this.putValue(NAME,String.format("Open %s log",importer.getName()));
  }

  public void actionPerformed(ActionEvent e) {
    JOtrosVfsBrowserDialog chooser = getOtrosApplication().getOtrosVfsBrowserDialog();
		initFileChooser(chooser);
    JOtrosVfsBrowserDialog.ReturnValue result = chooser.showOpenDialog((Component) e.getSource(),"Open " + importer.getName() + " log");
    if (result != JOtrosVfsBrowserDialog.ReturnValue.Approve) {
      return;
    }
    final FileObject[] files = chooser.getSelectedFiles();
    for (final FileObject file : files) {
      try {
        TableColumns[] tableColumnsToUse = TableColumns.ALL_WITHOUT_LOG_SOURCE;
        if (importer instanceof TableColumnNameSelfDescribable) {
          TableColumnNameSelfDescribable describable = (TableColumnNameSelfDescribable) importer;
          tableColumnsToUse = describable.getTableColumnsToUse();
        }

        final LoadingInfo openFileObject = Utils.openFileObject(file);
        final LogViewPanelWrapper panel = new LogViewPanelWrapper(file.getName().getBaseName(), openFileObject.getObserableInputStreamImpl(),
          tableColumnsToUse, getOtrosApplication());
        String tabName = Utils.getFileObjectShortName(file);
        getOtrosApplication().addClosableTab(tabName, file.getName().getFriendlyURI(), Icons.FOLDER_OPEN, panel, true);
        Runnable r = () -> {
          ProgressWatcher watcher = null;
          final ProxyLogDataCollector collector = new ProxyLogDataCollector();
          ParsingContext parsingContext = new ParsingContext(file.getName().getFriendlyURI(), file.getName().getBaseName());
          importStats = new LogImportStats(file.getName().getFriendlyURI());
          panel.getStatsTable().setModel(importStats);
          watcher = new ProgressWatcher(openFileObject.getObserableInputStreamImpl(), panel, file);
          Thread t = new Thread(watcher, "Log loader: " + file.getName().toString());
          t.setDaemon(true);
          t.start();
          panel.addHierarchyListener(new ReadingStopperForRemove(openFileObject.getObserableInputStreamImpl()));
          importer.initParsingContext(parsingContext);
          importer.importLogs(openFileObject.getContentInputStream(), collector, parsingContext);
          final LogDataTableModel dataTableModel = panel.getDataTableModel();
          LOGGER.info("File " + file.getName().getFriendlyURI() + " loaded");
          dataTableModel.add(collector.getLogData());
          SwingUtilities.invokeLater(panel::switchToContentView);
          watcher.updateFinish("Loaded");
          Utils.closeQuietly(openFileObject.getFileObject());
        };
        Thread t = new Thread(r);
        t.start();
      } catch (Exception e1) {
        LOGGER.error("Error loading log (" + file.getName().getFriendlyURI() + "): " + e1.getMessage());
        JOptionPane.showMessageDialog(null, "Error loading log: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }

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

  private class ProgressWatcher implements Runnable {

    private final ObservableInputStreamImpl in;
    private final JProgressBar progressBar;
    private boolean refreshProgress = true;
    private final LogViewPanelWrapper frame;
    private final FileObject fileName;
    private final NumberFormat npf = NumberFormat.getPercentInstance();
    private final NumberFormat nbf = NumberFormat.getIntegerInstance();

    public ProgressWatcher(ObservableInputStreamImpl in, LogViewPanelWrapper frame, FileObject fileName) {
      super();
      this.in = in;
      this.frame = frame;
      this.fileName = fileName;
      this.progressBar = frame.getLoadingProgressBar();
      npf.setMaximumFractionDigits(0);
      nbf.setGroupingUsed(true);
    }

    @Override
    public void run() {
      while (refreshProgress) {
        try {
          long max = fileName.getContent().getSize();
          if (max <= 0) {
            updateNotDetermined("Loading");
          } else {
            long current = in.getCurrentRead();
            float percent = (float) current / max;
            long currentInKb = current / 1024;
            long maxInKb = max / 1024;
            importStats.updateStats(System.currentTimeMillis(), currentInKb, maxInKb);
            String message = "Loading " + fileName.getName().getBaseName() + " ... " + npf.format(percent) + "[" + nbf.format(currentInKb) + "kb of "
                + nbf.format(maxInKb) + "kb]";
            updateProgress(message, (int) current, 0, (int) max);

          }
          Thread.sleep(500);
        } catch (InterruptedException e) {
          // waiting interrupted, nothing serious.

        } catch (IOException e) {
          // e.printStackTrace();
          // stream closed - all loaded
          break;
        }
      }
      updateFinish("Loaded");

    }

    public void updateNotDetermined(final String message) {
      Runnable r = () -> {
        progressBar.setIndeterminate(true);
        progressBar.setString(message);
      };

      SwingUtilities.invokeLater(r);
    }

    public void updateProgress(final String message, final int current, final int min, final int max) {
      Runnable r = () -> {
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(max);
        progressBar.setMinimum(min);
        progressBar.setValue(current);
        progressBar.setString(message);
        // refreshProgress = false;

      };
      try {
        SwingUtilities.invokeAndWait(r);
      } catch (InterruptedException | InvocationTargetException e) {
        e.printStackTrace();
      }

    }

    public void updateFinish(final String message) {
      Runnable r = () -> {
        progressBar.setIndeterminate(false);

        progressBar.setMaximum(1);
        progressBar.setMinimum(0);
        progressBar.setValue(1);
        progressBar.setString(message);
        frame.switchToContentView();
        refreshProgress = false;
      };
      SwingUtilities.invokeLater(r);

    }

  }

  private static class ReadingStopperForRemove implements HierarchyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadingStopperForRemove.class.getName());

    private final SoftReference<Stoppable> reference;

    public ReadingStopperForRemove(Stoppable stoppable) {
      super();
      reference = new SoftReference<>(stoppable);
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
      if (e.getChangeFlags() == 1 && e.getChanged().getParent() == null) {
        Stoppable stoppable = reference.get();
        LOGGER.debug("Tab removed, stopping thread if reference is != null (actual: " + stoppable + ")");
        if (stoppable != null) {
          stoppable.stop();
        }
      }

    }

  }
}
