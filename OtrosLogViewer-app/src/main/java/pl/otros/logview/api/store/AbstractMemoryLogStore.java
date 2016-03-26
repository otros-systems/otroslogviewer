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
package pl.otros.logview.api.store;

import pl.otros.logview.api.gui.NotableTableModel;
import pl.otros.logview.api.gui.NotableTableModelImpl;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;

import java.util.Comparator;
import java.util.TreeMap;

public abstract class AbstractMemoryLogStore implements LogDataStore {

  protected NotableTableModel notable;
  protected int limit = 2000000;
  protected volatile int idCounter = 0;
  protected Comparator<LogData> logDataTimeComparator;

  public AbstractMemoryLogStore() {
    notable = new NotableTableModelImpl();
    logDataTimeComparator = new LogDataTimeIdComparator();
  }

  @Override
  public boolean isMarked(int row) {
    return getLogData(row).isMarked();
  }

  @Override
  public MarkerColors getMarkerColors(int row) {
    return getLogData(row).getMarkerColors();
  }

  @Override
  public void markRows(MarkerColors markerColor, int... rows) {
    for (int row : rows) {
      getLogData(row).setMarked(true);
      getLogData(row).setMarkerColors(markerColor);
    }
  }

  @Override
  public void unmarkRows(int... rows) {
    for (int i = 0; i < rows.length; i++) {
      getLogData(rows[i]).setMarked(false);
      getLogData(rows[i]).setMarkerColors(null);
    }
  }

  @Override
  public int getLimit() {
    return limit;
  }

  @Override
  public void setLimit(int limit) {
    this.limit = limit;
    ensureLimit();
  }

  protected void ensureLimit() {
    if (limit <= getCount()) {
      int[] toDelete = new int[getCount() - limit];
      for (int i = 0; i < toDelete.length; i++) {
        toDelete[i] = i;
      }
      // TODO notify when some rows have to be removed!
      remove(toDelete);
    }
  }

  public void addNoteToRow(int row, Note note) {
    LogData logData = getLogData(row);
    logData.setNote(note);
    notable.addNoteToRow(logData.getId(), note);
  }

  public Note getNote(int row) {
    return getLogData(row).getNote();
  }

  public Note removeNote(int row) {
    getLogData(row).setNote(null);
    return notable.removeNote(row);
  }

  public void removeNote(int row, boolean notify) {
    getLogData(row).setNote(null);
    notable.removeNote(row, notify);
  }

  public void clearNotes() {
    int count = getCount();
    for (int i = 0; i < count; i++) {
      getLogData(i).setNote(null);
    }
    notable.clearNotes();
  }

  public TreeMap<Integer, Note> getAllNotes() {
    TreeMap<Integer, Note> result = new TreeMap<>();
    int count = getCount();
    for (int i = 0; i < count; i++) {
      Note note = getNote(i);
      if (note != null && note.getNote().length() > 0) {
        result.put(Integer.valueOf(i), note);
      }
    }
    return result;
  }

  protected int getNextLogId() {
    return idCounter++;
  }

  private static class LogDataTimeIdComparator implements Comparator<LogData> {

    @Override
    public int compare(LogData o1, LogData o2) {
      int compareTo = o1.getDate().compareTo(o2.getDate());
      if (compareTo == 0) {
        compareTo = o1.getId() - o2.getId();
      }
      return compareTo;
    }

  }

}
