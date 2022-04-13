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

import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;

import java.util.Iterator;
import java.util.TreeMap;

public class SynchronizedLogDataStore implements LogDataStore {

  protected LogDataStore logDataStore;

  public SynchronizedLogDataStore(LogDataStore logDataStore) {
    super();
    this.logDataStore = logDataStore;
  }

  public synchronized int getCount() {
    return logDataStore.getCount();
  }

  public synchronized void remove(int... ids) {
    logDataStore.remove(ids);
  }

  public synchronized Iterator<LogData> iterator() {
    return logDataStore.iterator();
  }

  public synchronized LogData getLogData(int row) {
    return logDataStore.getLogData(row);
  }

  public synchronized LogData[] getLogData() {
    return logDataStore.getLogData();
  }

  public synchronized Integer getLogDataIdInRow(int row) {
    return logDataStore.getLogDataIdInRow(row);
  }

  public synchronized int getLimit() {
    return logDataStore.getLimit();
  }

  public synchronized void setLimit(int limit) {
    logDataStore.setLimit(limit);
  }

  public synchronized int clear() {
    return logDataStore.clear();
  }

  public synchronized void add(LogData... logDatas) {
    logDataStore.add(logDatas);
  }

  public synchronized void addNoteToRow(int row, Note note) {
    logDataStore.addNoteToRow(row, note);
  }

  public synchronized boolean isMarked(int row) {
    return logDataStore.isMarked(row);
  }

  public synchronized Note getNote(int row) {
    return logDataStore.getNote(row);
  }

  public synchronized MarkerColors getMarkerColors(int row) {
    return logDataStore.getMarkerColors(row);
  }

  public synchronized Note removeNote(int row) {
    return logDataStore.removeNote(row);
  }

  public synchronized void markRows(MarkerColors markerColors, int... rows) {
    logDataStore.markRows(markerColors, rows);
  }

  public synchronized void clearNotes() {
    logDataStore.clearNotes();
  }

  public synchronized void unmarkRows(int... rows) {
    logDataStore.unmarkRows(rows);
  }

  public synchronized TreeMap<Integer, Note> getAllNotes() {
    return logDataStore.getAllNotes();
  }

}
