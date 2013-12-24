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

package pl.otros.logview.gui.message.update;

import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.LogViewPanel;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.note.NoteEvent;
import pl.otros.logview.gui.note.NoteObserver;
import pl.otros.logview.gui.util.DelayedSwingInvoke;
import pl.otros.logview.pluginable.PluginableElement;
import pl.otros.logview.pluginable.PluginableElementEventListener;
import pl.otros.logview.pluginable.PluginableElementsContainer;
import pl.otros.swing.rulerbar.RulerBarHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDetailListener implements ListSelectionListener, NoteObserver {

  private static final Logger LOGGER = Logger.getLogger(MessageDetailListener.class.getName());
  public static final int FORMAT_IN_SEPARATE_THREAD_THRESHOLD = 4 * 1024;

  private LogViewPanel logViewPanel;
  private JTable table;
  private JTextPane logDetailTextArea;
  private LogDataTableModel dataTableModel;
  private SimpleDateFormat dateFormat;
  private int maximumMessageSize = 400 * 1000;


  private final PluginableElementsContainer<MessageColorizer> colorizersContainer;
  private final PluginableElementsContainer<MessageFormatter> formattersContainer;

  private FormatMessageDialogWorker messageFormatSwingWorker;
  private DelayedSwingInvoke delayedSwingInvoke;

  public MessageDetailListener(LogViewPanel logViewPanel, SimpleDateFormat dateFormat,
                               PluginableElementsContainer<MessageFormatter> formattersContainer, PluginableElementsContainer<MessageColorizer> colorizersContainer) {
    super();
    this.logViewPanel = logViewPanel;
    this.table = logViewPanel.getTable();
    this.logDetailTextArea = logViewPanel.getLogDetailTextArea();
    this.dataTableModel = logViewPanel.getDataTableModel();
    this.dateFormat = dateFormat;
    this.formattersContainer = formattersContainer;
    this.colorizersContainer = colorizersContainer;

    formattersContainer.addListener(new PluginableElementEventListenerImplementation<MessageFormatter>());
    colorizersContainer.addListener(new PluginableElementEventListenerImplementation<MessageColorizer>());

    delayedSwingInvoke = new DelayedSwingInvoke() {
      @Override
      protected void performActionHook() {
        updateInfo();
      }
    };

  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    updateInfo();

  }

  @Override
  public void update(NoteEvent noteEvent) {
    updateInfo();
  }

  public void updateInfo() {
    if (messageFormatSwingWorker != null && messageFormatSwingWorker.getState() == SwingWorker.StateValue.STARTED) {
      messageFormatSwingWorker.cancel(true);
    }
    RulerBarHelper.clearMarkers(logViewPanel.getLogDetailWithRulerScrollPane());
    logDetailTextArea.setText("");
    int row = table.getSelectedRow();

    try {
      if (row >= 0 && row < table.getRowCount()) {
        int rowConverted = table.convertRowIndexToModel(row);
        LogData displayedLogData = dataTableModel.getLogData(rowConverted);
        logViewPanel.setDisplayedLogData(displayedLogData);
        messageFormatSwingWorker = new FormatMessageDialogWorker(displayedLogData, dateFormat, logViewPanel.getLogDetailWithRulerScrollPane(),
            colorizersContainer,
            formattersContainer, maximumMessageSize);
        if (displayedLogData.getMessage().length() > FORMAT_IN_SEPARATE_THREAD_THRESHOLD) {
          logDetailTextArea.setText("Updating log event details...");
          messageFormatSwingWorker.execute();
        } else {
          messageFormatSwingWorker.updateChanges(messageFormatSwingWorker.doInBackground());
        }

      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error formatting message details", e);
    }
    LOGGER.finer("Gui update call scheduled. Changes will be done in background");

  }

  public LogDataTableModel getDataTableModel() {
    return dataTableModel;
  }


  private final class PluginableElementEventListenerImplementation<T extends PluginableElement> implements PluginableElementEventListener<T> {

    @Override
    public void elementRemoved(T element) {
      updateInfo();
    }

    @Override
    public void elementChanged(T element) {
      updateInfo();
    }

    @Override
    public void elementAdded(T element) {
      updateInfo();
    }
  }

  public int getMaximumMessageSize() {
    return maximumMessageSize;
  }

  public void setMaximumMessageSize(int maximumMessageSize) {
    this.maximumMessageSize = maximumMessageSize;
    delayedSwingInvoke.performAction();
  }
}
