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
package pl.otros.logview.filter;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXHyperlink;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.pluginable.LogFilterValueChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class ThreadFilter extends AbstractLogFilter {
  private static final String NAME = "Thread Filter";
  private static final String DESCRIPTION = "Filtering events based on a thread.";
  private final JList<String> jList;
  private final Set<String> selectedThread;
  private final JPanel panel;
  private final DefaultListModel<String> listModel;

  public ThreadFilter() {
    super(NAME, DESCRIPTION);
    selectedThread = new HashSet<>();
    listModel = new DefaultListModel<>();
    jList = new JList<>(listModel);
    jList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    jList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        selectedThread.clear();
        Object[] selectedValues = jList.getSelectedValues();
        for (Object selectedValue : selectedValues) {
          selectedThread.add((String) selectedValue);
        }
        listener.ifPresent(LogFilterValueChangeListener::valueChanged);
      }
    });
    JLabel jLabel = new JLabel("Threads:");
    jLabel.setLabelFor(jList);
    jLabel.setDisplayedMnemonic('t');
    panel = new JPanel(new MigLayout());
    panel.add(jLabel, "wrap");
    panel.add(new JScrollPane(jList), "wrap, right, growx");
    panel.add(new JLabel("Use CTRL for multi selection"), "wrap");
    panel.add(new JXHyperlink(new AbstractAction("Invert selection") {
      @Override
      public void actionPerformed(ActionEvent e) {
        invertSelection();
      }
    }), "wrap");
    panel.add(new JXHyperlink(new AbstractAction("Clear selection") {
      @Override
      public void actionPerformed(ActionEvent e) {
        clearSelection();
      }
    }), "wrap");
    panel.add(new JXHyperlink(new AbstractAction("Reload threads") {
      @Override
      public void actionPerformed(ActionEvent e) {
        reloadThreads();
      }
    }), "wrap");
  }

  private void clearSelection() {
    selectedThread.clear();
    jList.clearSelection();
  }

  private void invertSelection() {
    int[] selectedIndices = jList.getSelectedIndices();
    ArrayList<Integer> inverted = new ArrayList<>();
    for (int i = 0; i < listModel.getSize(); i++) {
      inverted.add(i);
    }
    Arrays.sort(selectedIndices);
    ArrayUtils.reverse(selectedIndices);
    for (int selectedIndex : selectedIndices) {
      inverted.remove(selectedIndex);
    }
    int[] invertedArray = new int[inverted.size()];
    for (int i = 0; i < inverted.size(); i++) {
      invertedArray[i] = inverted.get(i);
    }
    jList.setSelectedIndices(invertedArray);
  }

  @Override
  public boolean accept(LogData logData, int row) {
    return (selectedThread.size() == 0 || selectedThread.contains(logData.getThread()));
  }

  @Override
  public Component getGUI() {
    return panel;
  }

  @Override
  public void init(Properties properties, LogDataTableModel collector) {
    this.collector = collector;
  }

  @Override
  public void setEnable(boolean enable) {
    super.setEnable(enable);
    if (enable) {
      reloadThreads();
    }
  }

  private void reloadThreads() {
    LogData[] ld = collector.getLogData();
    TreeSet<String> sortedThreads = new TreeSet<>(String::compareToIgnoreCase);
    for (LogData logData : ld) {
      sortedThreads.add(logData.getThread());
    }
    sortedThreads.stream()
      .filter(sortedThread -> !listModel.contains(sortedThread))
      .forEach(sortedThread -> listModel.add(listModel.getSize(), sortedThread));
    setThreadToFilter(selectedThread.toArray(new String[selectedThread.size()]));
  }

  public void setThreadToFilter(String... thread) {
    List<Integer> indexToSelect = new ArrayList<>();
    for (String s : thread) {
      for (int i = 0; i < listModel.getSize(); i++) {
        String elementAt = listModel.getElementAt(i);
        if (elementAt.equals(s)) {
          indexToSelect.add(i);
        }
      }
    }
    int[] indexes = new int[indexToSelect.size()];
    for (int i = 0; i < indexToSelect.size(); i++) {
      indexes[i] = indexToSelect.get(i);
    }
    jList.setSelectedIndices(indexes);
  }
}
