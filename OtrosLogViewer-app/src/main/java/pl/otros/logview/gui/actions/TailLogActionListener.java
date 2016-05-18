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
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.Stoppable;
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
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.parser.TableColumnNameSelfDescribable;
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
import java.util.Optional;

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

    final LogLoader logLoader = getOtrosApplication().getLogLoader();
    final VfsSource source = new VfsSource(file);
    final LogImporter logImporter = this.importer;
    final LogLoadingSession session = logLoader.startLoading(
      source,
      logImporter,
      panel.getDataTableModel(),
      3000,
      Optional.of(2000L)
    );
    panel.onClose(()->logLoader.close(session));
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
