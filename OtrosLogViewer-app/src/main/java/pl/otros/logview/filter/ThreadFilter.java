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
package pl.otros.logview.filter;

import pl.otros.logview.LogData;
import pl.otros.logview.gui.LogDataTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeSet;

public class ThreadFilter extends AbstractLogFilter {

  private static final String NAME = "Thread Filter";
  private static final String DESCRIPTION = "Filtering events based on a thread.";
  private JComboBox gui;
  private DefaultComboBoxModel boxModel;
  private static final String ALL_THREADS = "--ALL--";

  public ThreadFilter() {
    super(NAME, DESCRIPTION);
    boxModel = new DefaultComboBoxModel();
    boxModel.addElement(ALL_THREADS);
    gui = new JComboBox(boxModel);

    gui.setOpaque(true);
    gui.setEditable(false);
    gui.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        listener.valueChanged();
      }
    });

  }

  @Override
  public boolean accept(LogData logData, int row) {
    if (gui.getSelectedIndex() == 0 || logData.getThread().equals(boxModel.getSelectedItem())) {
      return true;
    }
    return false;
  }

  @Override
  public Component getGUI() {
    return gui;
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

    TreeSet<String> sortedThreads = new TreeSet<String>(new Comparator<String>() {

      @Override
      public int compare(String arg0, String arg1) {
        return arg0.compareToIgnoreCase(arg1);
      }
    });

    for (LogData logData : ld) {
      sortedThreads.add(logData.getThread());
    }

    Object selectedItem = boxModel.getSelectedItem();

    boxModel.removeAllElements();
    boxModel.addElement(ALL_THREADS);

    for (String thread : sortedThreads) {
      boxModel.addElement(thread);
    }

    if (selectedItem != null) {
      boxModel.setSelectedItem(selectedItem);
    }
  }

  public void setThreadToFilter(String thread) {
    boxModel.setSelectedItem(thread);
  }

}
