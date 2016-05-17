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
package pl.otros.logview.api.gui;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.Stoppable;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.loading.LoadStatistic;
import pl.otros.logview.api.loading.LoadingDetails;
import pl.otros.logview.api.loading.LogLoader;
import pl.otros.logview.api.loading.LogLoadingSession;
import pl.otros.logview.gui.LogViewPanel;
import pl.otros.logview.gui.TailingModeMarkersPanel;
import pl.otros.logview.gui.actions.ClearLogTableAction;
import pl.otros.vfs.browser.table.FileSize;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogViewPanelWrapper extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogViewPanelWrapper.class.getName());

  private String name;
  private LogDataTableModel dataTableModel = new LogDataTableModel();
  private final LogViewPanelI logViewPanel;
  private final CardLayout cardLayout;
  private static final String CARD_LAYOUT_LOADING = "card layout loading";
  private static final String CARD_LAYOUT_CONTENT = "card layout content";
  private final JProgressBar loadingProgressBar;
  private JCheckBox follow;
  private JCheckBox playTailing;
  private DataConfiguration configuration;
  private final OtrosApplication otrosApplication;
  private Optional<Runnable> onCloseAction = Optional.empty();
  private final LogLoader logLoader;
  private Optional<Timer> timer = Optional.empty();
  private JProgressBar progressBar;


  public LogViewPanelWrapper(String name, Stoppable stoppable, TableColumns[] visibleColumns, OtrosApplication otrosApplication) {
    this(name, stoppable, visibleColumns, new LogDataTableModel(), otrosApplication);
  }

  public LogViewPanelWrapper(String name, Stoppable stoppable, TableColumns[] visibleColumns, LogDataTableModel logDataTableModel, OtrosApplication otrosApplication) {
    this(name, stoppable, visibleColumns, logDataTableModel, new DataConfiguration(new BaseConfiguration()), otrosApplication);
  }

  public LogViewPanelWrapper(
    final String name,
    final Stoppable stoppable,
    final TableColumns[] visibleColumns,
    final LogDataTableModel logDataTableModel,
    final DataConfiguration configuration,
    final OtrosApplication otrosApplication) {

    this.name = name;
    this.configuration = configuration;
    this.otrosApplication = otrosApplication;
    logLoader = otrosApplication.getLogLoader();
    this.addHierarchyListener(e -> {
      if (e.getChangeFlags() == 1 && e.getChanged().getParent() == null) {
        closing();
      }
    });
    final TableColumns[] columns = (visibleColumns == null) ? TableColumns.ALL_WITHOUT_LOG_SOURCE : visibleColumns;

    fillDefaultConfiguration();

    SoftReference<Stoppable> stoppableReference = new SoftReference<>(stoppable);
    // this.statusObserver = statusObserver;
    dataTableModel = logDataTableModel == null ? new LogDataTableModel() : logDataTableModel;
    logViewPanel = new LogViewPanel(dataTableModel, columns, otrosApplication);

    cardLayout = new CardLayout();
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(10, 10, 10, 10);
    c.anchor = GridBagConstraints.PAGE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.ipadx = 1;
    c.ipady = 1;
    c.weightx = 10;
    c.weighty = 1;

    c.gridy++;
    c.weighty = 3;
    loadingProgressBar = new JProgressBar();
    loadingProgressBar.setIndeterminate(false);
    loadingProgressBar.setStringPainted(true);
    loadingProgressBar.setString("Connecting...");

    c.gridy++;
    c.weighty = 1;
    c.weightx = 2;
    c.gridy++;
    c.weightx = 1;
    JButton stopButton = new JButton("Stop, you have imported already enough!");
    stopButton.addActionListener(e -> {
      Stoppable stoppable1 = stoppableReference.get();
      if (stoppable1 != null) {
        stoppable1.stop();
      }
    });

    setLayout(cardLayout);
    add(logViewPanel, CARD_LAYOUT_CONTENT);
    cardLayout.show(this, CARD_LAYOUT_LOADING);

  }

  private void closing() {
    LOGGER.info("Log view panel is removed from view. Clearing data table for GC and running onCloseAction action");
    dataTableModel.clear();
    onCloseAction.ifPresent(Runnable::run);
    timer.ifPresent(Timer::stop);
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LogDataTableModel getDataTableModel() {
    return dataTableModel;
  }

  public void setDataTableModel(LogDataTableModel dataTableModel) {
    this.dataTableModel = dataTableModel;
  }

  public LogViewPanelI getLogViewPanel() {
    return logViewPanel;
  }

  @Override
  public String toString() {
    return name;
  }

  public JProgressBar getLoadingProgressBar() {
    return loadingProgressBar;
  }

  public void switchToContentView() {
    cardLayout.show(this, CARD_LAYOUT_CONTENT);

  }

  private void fillDefaultConfiguration() {
    configuration.addProperty(ConfKeys.TAILING_PANEL_FOLLOW, true);
    configuration.addProperty(ConfKeys.TAILING_PANEL_PLAY, true);
  }

  public DataConfiguration getConfiguration() {
    return configuration;
  }

  public void goToLiveMode() {
    JToolBar jToolBar = new JToolBar();
    createFollowCheckBox();
    createPauseCheckBox();
    createReadingProgressBar();

    jToolBar.add(playTailing);
    jToolBar.add(follow);
    JButton clear = new JButton(new ClearLogTableAction(otrosApplication));
    clear.setBorderPainted(false);
    jToolBar.add(clear);
    jToolBar.add(progressBar);

    logViewPanel.add(jToolBar, BorderLayout.NORTH);
    logViewPanel.getLogsMarkersPanel().setLayout(new BorderLayout());
    TailingModeMarkersPanel markersPanel = new TailingModeMarkersPanel(logViewPanel.getDataTableModel());
    logViewPanel.getLogsMarkersPanel().add(markersPanel);

    switchToContentView();
    addRowAutoScroll();
  }

  private void createPauseCheckBox() {
    boolean play = configuration.getBoolean(ConfKeys.TAILING_PANEL_PLAY);
    playTailing = new JCheckBox("", play ? Icons.TAILING_LIVE : Icons.TAILING_PAUSE, play);
    playTailing.setToolTipText("Pause adding new data");
    playTailing.addActionListener(e -> {
      boolean play1 = playTailing.isSelected();
      playTailing.setIcon(play1 ? Icons.TAILING_LIVE : Icons.TAILING_PAUSE);
      final LoadingDetails loadingDetails = logLoader.getLoadingDetails(dataTableModel);
      final Stream<LogLoadingSession> stream = loadingDetails.getLogLoadingSessions().stream();
      if (play1) {
        stream.forEach(logLoader::resume);
      } else {
        stream.forEach(logLoader::pause);
      }
    });
  }

  private void createFollowCheckBox() {
    boolean f = configuration.getBoolean(ConfKeys.TAILING_PANEL_FOLLOW);
    follow = new JCheckBox("Follow new events", f ? Icons.FOLLOW_ON : Icons.FOLLOW_OFF, f);
    follow.setToolTipText("Scroll to latest log event");
    follow.addActionListener(e -> {
      boolean f1 = follow.isSelected();
      follow.setIcon(f1 ? Icons.FOLLOW_ON : Icons.FOLLOW_OFF);
      configuration.setProperty(ConfKeys.TAILING_PANEL_FOLLOW, f1);
    });

  }

  private void createReadingProgressBar() {
    progressBar = new JProgressBar(0, 100);
    progressBar.setStringPainted(true);
    progressBar.setString("Processed ? of ? [?%]");
    final Timer t = new Timer(500, e -> {
      LOGGER.trace("Updating reading progress");
      final LoadingDetails loadingDetails = logLoader.getLoadingDetails(dataTableModel);
      final List<LogLoadingSession> logLoadingSessions = loadingDetails.getLogLoadingSessions();
      final List<LoadStatistic> statistics = logLoadingSessions
        .stream()
        .map(logLoader::getLoadStatistic)
        .collect(Collectors.toList());
      final Long position = statistics.stream().collect(Collectors.summingLong(LoadStatistic::getPosition));
      final Long total = statistics.stream().collect(Collectors.summingLong(LoadStatistic::getTotal));
      final float percent = (100f) * ((float) position / total);
      progressBar.setValue((int) percent);
      final String msg = String.format("Processed %s of %s [%.2f%%]",
        FileSize.convertToStringRepresentation(position),
        FileSize.convertToStringRepresentation(total),
        percent);
      LOGGER.trace("Updating progress bar with message {}", msg);
      progressBar.setString(msg);


      final String tooltip = "<HTML>" + statistics.stream()
        .map(s -> String.format("Processed %s of %s [%.2f%%]  - %s",
          FileSize.convertToStringRepresentation(s.getPosition()),
          FileSize.convertToStringRepresentation(s.getTotal()),
          s.getPercent(),
          s.getSource().stringForm()))
        .collect(Collectors.joining("<BR/>"))
        +"</HTML>";
      progressBar.setToolTipText(tooltip);
    });
    t.setRepeats(true);
    t.setInitialDelay(1000);
    t.start();
    timer = Optional.of(t);
  }

  private void addRowAutoScroll() {
    dataTableModel.addTableModelListener(e -> {
      if (follow.isSelected() && e.getType() == TableModelEvent.INSERT) {
        final Runnable r = () -> {
          try {
            JTable table = logViewPanel.getTable();
            int row = table.getRowCount() - 1;
            if (row > 0) {
              Rectangle rect = table.getCellRect(row, 0, true);
              table.scrollRectToVisible(rect);
              table.clearSelection();
              table.setRowSelectionInterval(row, row);
            }
          } catch (IllegalArgumentException iae) {
            // ignore..out of bounds
            iae.printStackTrace();
          }
        };
        // Wait for JViewPort size update
        // TODO Find way to invoke this listener after viewport is notified about changes
        Runnable r2 = () -> {
          try {
            Thread.sleep(300);
          } catch (InterruptedException ignore) {
          }
          SwingUtilities.invokeLater(r);
        };
        new Thread(r2).start();
      }
    });
  }

  public void onClose(Runnable r) {
    onCloseAction = Optional.of(r);
  }

  public void setConfiguration(DataConfiguration configuration) {
    this.configuration = configuration;
  }


}
