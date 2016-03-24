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

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.Note;
import pl.otros.logview.api.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class NoteRenderer extends DefaultTableCellRenderer {

  private final ImageIcon iconEmpty;
  private final ImageIcon iconExist;
  private final JLabel label;

  public NoteRenderer() {
    label = new JLabel();
    iconEmpty = Icons.NOTE_EMPTY;
    iconExist = Icons.NOTE_EXIST;
    label.setOpaque(true);
    setIcon(iconEmpty);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    // super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (value instanceof Note) {
      Note note = (Note) value;
      if (StringUtils.isBlank(note.getNote())) {
        label.setIcon(iconEmpty);
        label.setText("");
      } else {
        label.setText(note.getNote());
        label.setIcon(iconExist);
      }
    }
    return label;
  }

}
