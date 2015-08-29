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
package pl.otros.logview.gui.renderers;

import pl.otros.logview.Note;

import javax.swing.*;
import java.awt.*;

public class NoteTableEditor extends DefaultCellEditor {

  private final JTextField tf;

  public NoteTableEditor() {
    super(new JTextField());
    tf = (JTextField) getComponent();
  }

  @Override
  public Object getCellEditorValue() {
    Note n = new Note(tf.getText());
    return n;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    Note n = ((Note) value);
    String text = n != null && n.getNote() != null ? n.getNote() : "";
    tf.setText(text);
    return c;
  }

}
