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

import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.Note;
import pl.otros.logview.api.gui.LogDataTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class MarkNoteFilter extends AbstractLogFilter {

  private static final String NAME = "Mark/Note filter";
  private static final String DESCRIPTION = "Filtering events based on a mark or a note.";
  private final JComboBox noteComboBox = new JComboBox(new Object[] { "N/A", "Yes", "No" });
  private final JComboBox markComboBox = new JComboBox(new Object[] { "N/A", "Yes", "No" });
  private final JPanel gui;

  public MarkNoteFilter() {
    super(NAME, DESCRIPTION);
    noteComboBox.setBorder(BorderFactory.createTitledBorder("With note:"));
    noteComboBox.addActionListener(e -> listener.valueChanged());

    markComboBox.setBorder(BorderFactory.createTitledBorder("Marked:"));
    markComboBox.addActionListener(e -> listener.valueChanged());

    gui = new JPanel(new GridLayout(2, 1));
    gui.add(noteComboBox);
    gui.add(markComboBox);
  }

  @Override
  public boolean accept(LogData logData, int row) {
    boolean accept = true;
    boolean marked = logData.isMarked();
    Note n = logData.getNote();
    if (noteComboBox.getSelectedIndex() == 1) {
      accept = n != null && n.getNote().length() > 0;
    } else if (noteComboBox.getSelectedIndex() == 2) {
      accept = n == null || n.getNote().length() == 0;
    }
    if (markComboBox.getSelectedIndex() == 1) {
      accept = accept && marked;
    } else if (markComboBox.getSelectedIndex() == 2) {
      accept = accept && (!marked);
    }
    return accept;
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
    markComboBox.setEnabled(enable);
    noteComboBox.setEnabled(enable);
  }

}
