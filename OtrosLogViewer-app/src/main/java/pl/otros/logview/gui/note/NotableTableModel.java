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
package pl.otros.logview.gui.note;

import pl.otros.logview.Note;

import java.util.TreeMap;

public interface NotableTableModel {

  void addNoteToRow(int row, Note note);

  Note getNote(int row);

  Note removeNote(int row);

  Note removeNote(int row, boolean notify);

  void clearNotes();

  TreeMap<Integer, Note> getAllNotes();

  void addNoteObserver(NoteObserver observer);

  void removeNoteObserver(NoteObserver observer);

  void removeAllNoteObserver();

  void notifyAllNoteObservers(NoteEvent noteEvent);

}
