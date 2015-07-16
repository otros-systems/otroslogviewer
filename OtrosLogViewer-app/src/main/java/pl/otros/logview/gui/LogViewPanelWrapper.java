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
package pl.otros.logview.gui;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.Stoppable;
import pl.otros.logview.gui.actions.ClearLogTableAction;
import pl.otros.logview.gui.table.TableColumns;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.lang.ref.SoftReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogViewPanelWrapper extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogViewPanelWrapper.class.getName());

  private String name;
  private LogDataTableModel dataTableModel = new LogDataTableModel();
  private LogViewPanel logViewPanel;
  private CardLayout cardLayout;
  private static final String CARD_LAYOUT_LOADING = "card layout loading";
  private static final String CARD_LAYOUT_CONTENT = "card layout content";
  private JProgressBar loadingProgressBar;
  private JTable statsTable;
  private JButton stopButton;
  private JCheckBox follow;
  private SoftReference<Stoppable> stopableReference;
  private Mode mode = Mode.Static;
  private JCheckBox playTailing;
  private DataConfiguration configuration;
	private OtrosApplication otrosApplication;

	public enum Mode {
    Static, Live
  }


  public LogViewPanelWrapper(String name, Stoppable stoppable, TableColumns[] visibleColumns, OtrosApplication otrosApplication) {
		this(name,stoppable,visibleColumns,new LogDataTableModel(),otrosApplication);
  }

  public LogViewPanelWrapper(String name, Stoppable stoppable, TableColumns[] visibleColumns, LogDataTableModel logDataTableModel, OtrosApplication otrosApplication) {
    this(name, stoppable, visibleColumns, logDataTableModel, new DataConfiguration(new BaseConfiguration()),otrosApplication);
	}

  public LogViewPanelWrapper(String name, Stoppable stoppable, TableColumns[] visibleColumns,
      LogDataTableModel logDataTableModel, DataConfiguration configuration, OtrosApplication otrosApplication) {
    this.name = name;
    this.configuration = configuration;
		this.otrosApplication = otrosApplication;
		this.addHierarchyListener(new HierarchyListener() {

      @Override
      public void hierarchyChanged(HierarchyEvent e) {
        if (e.getChangeFlags() == 1 && e.getChanged().getParent() == null) {
          LOGGER.info("Log view panel is removed from view. Clearing data table for GC");
          dataTableModel.clear();
        }
      }
    });
    if (visibleColumns == null) {
      visibleColumns = TableColumns.ALL_WITHOUT_LOG_SOURCE;
    }

    fillDefaultConfiguration();

    stopableReference = new SoftReference<Stoppable>(stoppable);
    // this.statusObserver = statusObserver;
    dataTableModel = logDataTableModel == null ? new LogDataTableModel() : logDataTableModel;
    logViewPanel = new LogViewPanel(dataTableModel, visibleColumns,otrosApplication);

    cardLayout = new CardLayout();
    JPanel panelLoading = new JPanel(new GridBagLayout());
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

    JLabel label = new JLabel("Loading file " + name);
    panelLoading.add(label, c);
    c.gridy++;
    c.weighty = 3;
    loadingProgressBar = new JProgressBar();
    loadingProgressBar.setIndeterminate(false);
    loadingProgressBar.setStringPainted(true);
    loadingProgressBar.setString("Connecting...");
    panelLoading.add(loadingProgressBar, c);
    statsTable = new JTable();

    c.gridy++;
    c.weighty = 1;
    c.weightx = 2;
    panelLoading.add(statsTable, c);
    c.gridy++;
    c.weightx = 1;
    stopButton = new JButton("Stop, you have imported already enough!");
    stopButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        Stoppable stoppable = stopableReference.get();
        if (stoppable != null) {
          stoppable.stop();
        }
      }
    });
    panelLoading.add(stopButton, c);

    setLayout(cardLayout);
    add(panelLoading, CARD_LAYOUT_LOADING);
    add(logViewPanel, CARD_LAYOUT_CONTENT);
    cardLayout.show(this, CARD_LAYOUT_LOADING);

  }

  public JTable getStatsTable() {
    return statsTable;
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

  public LogViewPanel getLogViewPanel() {
    return logViewPanel;
  }

  public void setLogViewPanel(LogViewPanel logViewPanel) {
    this.logViewPanel = logViewPanel;
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
    mode = Mode.Live;
    JToolBar jToolBar = new JToolBar();
    createFollowCheckBox();
    createPauseCheckBox();

    jToolBar.add(playTailing);
    jToolBar.add(follow);
    JButton clear = new JButton(new ClearLogTableAction(otrosApplication));
    clear.setBorderPainted(false);
    jToolBar.add(clear);

    logViewPanel.add(jToolBar, BorderLayout.NORTH);
    logViewPanel.getLogsMarkersPanel().setLayout(new BorderLayout());
    TailingModeMarkersPanel markersPanel = new TailingModeMarkersPanel(logViewPanel.getDataTableModel());
    logViewPanel.getLogsMarkersPanel().add(markersPanel);

    switchToContentView();
    addRowScroller();
  }

  private void createPauseCheckBox() {
    boolean play = configuration.getBoolean(ConfKeys.TAILING_PANEL_PLAY);
    playTailing = new JCheckBox("", play ? Icons.TAILING_LIVE : Icons.TAILING_PAUSE, play);
    playTailing.setToolTipText("Pause adding new data");
    playTailing.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        boolean play = playTailing.isSelected();
        playTailing.setIcon(play ? Icons.TAILING_LIVE : Icons.TAILING_PAUSE);
        configuration.setProperty(ConfKeys.TAILING_PANEL_PLAY, play);
      }
    });
  }

  private void createFollowCheckBox() {
    boolean f = configuration.getBoolean(ConfKeys.TAILING_PANEL_FOLLOW);
    follow = new JCheckBox("Follow new events", f ? Icons.FOLLOW_ON : Icons.FOLLOW_OFF, f);
    follow.setToolTipText("Scroll to latest log event");
    follow.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        boolean f = follow.isSelected();
        follow.setIcon(f ? Icons.FOLLOW_ON : Icons.FOLLOW_OFF);
        configuration.setProperty(ConfKeys.TAILING_PANEL_FOLLOW, f);
      }
    });

  }

  private void addRowScroller() {
    dataTableModel.addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(final TableModelEvent e) {
        if (follow.isSelected() && e.getType() == TableModelEvent.INSERT) {
          final Runnable r = new Runnable() {

            @Override
            public void run() {
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
            }
          };
          // Wait for JViewPort size update
          // TODO Find way to invoke this listener after viewport is notified about changes
          Runnable r2 = new Runnable() {

            @Override
            public void run() {
              try {
                Thread.sleep(300);
              } catch (InterruptedException ignore) {
              }
              SwingUtilities.invokeLater(r);
            }
          };
          new Thread(r2).start();
        }
      }
    });
  }

  public void setConfiguration(DataConfiguration configuration) {
    this.configuration = configuration;
  }
}
