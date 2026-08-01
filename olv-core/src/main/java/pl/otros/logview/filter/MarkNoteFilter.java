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
import pl.otros.logview.api.filter.AbstractLogFilter;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.Note;

import javax.swing.*;
import java.awt.*;

public class MarkNoteFilter extends AbstractLogFilter {

  private static final String NAME = "Mark/Note filter";
  private static final String DESCRIPTION = "Filtering events based on a mark or a note.";
  private final JComboBox<String> noteComboBox = new JComboBox<>(new String[]{ "N/A", "Yes", "No" });
  private final JComboBox<String> markComboBox = new JComboBox<>(new String[]{ "N/A", "Yes", "No" });
  private final JPanel gui;

  public MarkNoteFilter() {
    super(NAME, DESCRIPTION);
    noteComboBox.addActionListener(e -> performValueChangedListener());

    markComboBox.addActionListener(e -> performValueChangedListener());

    JLabel noteLabel = new JLabel("With note:");
    noteLabel.setLabelFor(noteComboBox);

    JLabel markedLabel = new JLabel("Marked:");
    markedLabel.setLabelFor(markComboBox);

    gui = new JPanel(new MigLayout("insets 0, gapy 2"));
    gui.add(noteLabel, "growx");
    gui.add(noteComboBox, "right, wrap, growx");
    gui.add(markedLabel, "growx");
    gui.add(markComboBox, "right, wrap, growx");
  }

  @Override
  public boolean accept(LogData logData, int row) {
    boolean accept = true;
    boolean marked = logData.isMarked();
    Note n = logData.getNote();
    if (noteComboBox.getSelectedIndex() == 1) {
      accept = n != null && !n.getNote().isEmpty();
    } else if (noteComboBox.getSelectedIndex() == 2) {
      accept = n == null || n.getNote().isEmpty();
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
  public void setEnable(boolean enable) {
    super.setEnable(enable);
    markComboBox.setEnabled(enable);
    noteComboBox.setEnabled(enable);
  }

  @Override
  public boolean isFilterIsActive() {
    return noteComboBox.getSelectedIndex() != 0 || markComboBox.getSelectedIndex() != 0;
  }
}
