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
package pl.otros.logview.gui;

import pl.otros.logview.api.NoteObserver;
import pl.otros.logview.api.gui.NotableTableModel;
import pl.otros.logview.api.gui.NoteEvent;
import pl.otros.logview.api.model.Note;

import javax.swing.*;
import java.util.TreeMap;

public class AllNotesTextAreaObserver implements NoteObserver {

  private final JTextArea allNotesTextArea;

  public AllNotesTextAreaObserver(JTextArea allNotesTextArea) {
    super();
    this.allNotesTextArea = allNotesTextArea;
  }

  @Override
  public void update(NoteEvent noteEvent) {
    StringBuilder sb = new StringBuilder();
    NotableTableModel model = noteEvent.getSource();
    TreeMap<Integer, Note> allNotes = model.getAllNotes();
    for (Integer idx : allNotes.keySet()) {
      Note note = allNotes.get(idx);
      sb.append(idx).append(": ").append(note.getNote());
      sb.append('\n');
    }
    if (sb.length() == 0) {
      sb.append("No notes.");
    }
    allNotesTextArea.setText(sb.toString());

  }

}
