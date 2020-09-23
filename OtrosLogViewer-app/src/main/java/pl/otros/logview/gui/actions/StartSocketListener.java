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

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.BaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.BufferingLogDataCollectorProxy;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelWrapper;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.pluginable.AllPluginables;
import pl.otros.logview.reader.SocketLogReader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

public class StartSocketListener extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(StartSocketListener.class);
  private Collection<SocketLogReader> logReaders;
  private BufferingLogDataCollectorProxy logDataCollector;

  private LogViewPanelWrapper logViewPanelWrapper;

  public StartSocketListener(OtrosApplication otrosApplication, Collection<SocketLogReader> logReaders) {
    super(otrosApplication);
    this.logReaders = logReaders;
    putValue(Action.NAME, "Start socket listener");
    putValue(Action.SHORT_DESCRIPTION, "Start socket listener on port.");
    putValue(Action.LONG_DESCRIPTION, "Start socket listener on port.");
    putValue(SMALL_ICON, Icons.PLUGIN_PLUS);

  }

  @Override
  protected void actionPerformedHook(ActionEvent arg0) {

    LogImporterAndPort chooseLogImporter = chooseLogImporter();
    if (chooseLogImporter == null) {
      return;
    }

    StatusObserver observer = getOtrosApplication().getStatusObserver();
    if (logViewPanelWrapper == null) {
      logViewPanelWrapper = new LogViewPanelWrapper("Socket", null, TableColumns.values(), getOtrosApplication());

      logViewPanelWrapper.goToLiveMode();
      BaseConfiguration configuration = new BaseConfiguration();
      configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
      configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);
      long sleepTime = this.getOtrosApplication().getConfiguration().getLong(ConfKeys.READER_SOCKET_BUFFER_TIME,4000L);
      logDataCollector = new BufferingLogDataCollectorProxy(logViewPanelWrapper.getDataTableModel(), sleepTime, configuration);
    }


    getOtrosApplication().addClosableTab("Socket listener", "Socket listener", Icons.PLUGIN_CONNECT, logViewPanelWrapper, true);

    //TODO solve this warning
    SocketLogReader logReader = null;
    if (logReader == null || logReader.isClosed()) {
      logReader = new SocketLogReader(chooseLogImporter.logImporter, logDataCollector, observer, getOtrosApplication().getLogLoader(), chooseLogImporter.port);

      try {
        logReader.start();
        logReaders.add(logReader);
        observer.updateStatus(String.format("Socket opened on port %d with %s.", chooseLogImporter.port, chooseLogImporter.logImporter));
      } catch (Exception e) {
        LOGGER.error("Failed to open Socket listener", e);
        observer.updateStatus("Failed to open listener " + e.getMessage(), StatusObserver.LEVEL_ERROR);
      }
    }
  }

  private LogImporterAndPort chooseLogImporter() {
    Collection<LogImporter> elements = AllPluginables.getInstance().getLogImportersContainer().getElements();
    LogImporter[] importers = elements.toArray(new LogImporter[elements.size()]);
    String[] names = new String[elements.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = importers[i].getName();
    }

    JComboBox box = new JComboBox(names);
    box.setName("StartSocketListenerDialog.importer");
    SpinnerNumberModel numberModel = new SpinnerNumberModel(50505, 1025, 65000, 1);
    JSpinner jSpinner = new JSpinner(numberModel);
    jSpinner.setName("StartSocketListenerDialog.port");
    MigLayout migLayout = new MigLayout();
    JPanel panel = new JPanel(migLayout);
    panel.add(new JLabel("Select log importer"));
    panel.add(box, "wrap");
    panel.add(new JLabel("Select port"));
    panel.add(jSpinner, "span");

    if (logReaders.size() > 0) {
      panel.add(new JLabel("Opened sockets"), "wrap, growx");
      JTable jTable = new JTable(logReaders.size(), 2);
      jTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Log importer");
      jTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Port");
      int row = 0;
      for (SocketLogReader socketLogReader : logReaders) {
        jTable.setValueAt(socketLogReader.getLogImporter().getName(), row, 0);
        jTable.setValueAt(Integer.toString(socketLogReader.getPort()), row, 1);
        row++;
      }
      JScrollPane jScrollPane = new JScrollPane(jTable);
      panel.add(jScrollPane, "wrap, span");
    }
    int showConfirmDialog = JOptionPane.showConfirmDialog(null, panel, "Choose log importer and port", JOptionPane.OK_CANCEL_OPTION);
    if (showConfirmDialog != JOptionPane.OK_OPTION) {
      return null;
    }

    return new LogImporterAndPort(importers[box.getSelectedIndex()], numberModel.getNumber().intValue());
  }

  public static class LogImporterAndPort {

    private final int port;
    private final LogImporter logImporter;

    public LogImporterAndPort(LogImporter logImporter, int port) {
      this.logImporter = logImporter;
      this.port = port;
    }

    public int getPort() {
      return port;
    }

    public LogImporter getLogImporter() {
      return logImporter;
    }

  }

}
