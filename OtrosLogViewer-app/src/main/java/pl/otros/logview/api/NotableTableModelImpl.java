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
package pl.otros.logview.api;

import pl.otros.logview.api.NoteEvent.EventType;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class NotableTableModelImpl implements NotableTableModel {

  private final TreeMap<Integer, Note> notes;
  private static final Note EMPTY_NOTE = new Note("");
  private final Set<NoteObserver> noteObservers;

  public NotableTableModelImpl() {
    notes = new TreeMap<>();
    noteObservers = new HashSet<>();
  }

  @Override
  public void addNoteToRow(int row, Note note) {
    if (note == null || note.getNote() == null || note.getNote().length() == 0) {
      removeNote(row);
    } else {
      notes.put(row, note);
      NoteEvent event = new NoteEvent(EventType.ADD, this, note, row);
      notifyAllNoteObservers(event);
    }
  }

  @Override
  public void clearNotes() {
    notes.clear();
    notifyAllNoteObservers(new NoteEvent(EventType.CLEAR, this, null, 0));
  }

  @Override
  public TreeMap<Integer, Note> getAllNotes() {
    TreeMap<Integer, Note> copy = new TreeMap<>();
    copy.putAll(notes);
    return copy;
  }

  @Override
  public Note getNote(int row) {
    Note n = notes.get(row);
    n = n != null ? n : EMPTY_NOTE;
    return n;
  }

  @Override
  public Note removeNote(int row) {
    return removeNote(row, true);
  }

  @Override
  public void addNoteObserver(NoteObserver observer) {
    noteObservers.add(observer);
  }

  @Override
  public void removeAllNoteObserver() {
    noteObservers.clear();

  }

  @Override
  public void removeNoteObserver(NoteObserver observer) {
    noteObservers.remove(observer);
  }

  @Override
  public void notifyAllNoteObservers(NoteEvent noteEvent) {
    for (NoteObserver observer : noteObservers) {
      observer.update(noteEvent);
    }
  }

  @Override
  public Note removeNote(int row, boolean notify) {
    Note n = notes.remove(row);
    if (notify) {
      NoteEvent event = new NoteEvent(EventType.REMOVE, this, n, row);
      notifyAllNoteObservers(event);
    }
    return n;
  }

}
