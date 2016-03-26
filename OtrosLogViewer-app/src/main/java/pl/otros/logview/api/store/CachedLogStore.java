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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;
import pl.otros.logview.api.store.file.FileLogDataStore;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CachedLogStore implements LogDataStore {

  protected FileLogDataStore fileLogDataStore;
  // protected ConcurrentMap<Integer, LogData> cache;
  protected Cache<Integer, LogData> cache;
  private final int initialCapicity = 5000;
  private final long maximumSize = 20000;

  public CachedLogStore(FileLogDataStore fileLogDataStore) {
    super();
    this.fileLogDataStore = fileLogDataStore;
    CacheLoader<Integer, LogData> cacheLoader = new CacheLoader<Integer, LogData>() {

      @Override
      public LogData load(Integer arg0) throws Exception {
        return CachedLogStore.this.fileLogDataStore.getLogData(arg0.intValue());
      }

    };
    cache = CacheBuilder.newBuilder().weakValues().//
      weakKeys().//
      initialCapacity(initialCapicity).//
      expireAfterAccess(10, TimeUnit.MINUTES).//
      maximumSize(maximumSize).//

      build(cacheLoader);
  }

  @Override
  public int getCount() {
    return fileLogDataStore.getCount();
  }

  @Override
  public void add(LogData... logDatas) {
    // LogData's have to be added to FileLogDataStore.
    // FileLogDataSotre will generate unique id for LogData's
    fileLogDataStore.add(logDatas);
    for (LogData logData : logDatas) {
      cache.put(Integer.valueOf(logData.getId()), logData);
    }

  }

  @Override
  public void remove(int... rows) {
    for (int row : rows) {
      cache.invalidate(fileLogDataStore.getLogDataIdInRow(row));
    }
    fileLogDataStore.remove(rows);

  }

  @Override
  public LogData getLogData(final int row) {
    final Integer logDataIdInRow = fileLogDataStore.getLogDataIdInRow(row);
    LogData logData = null;
    try {
      logData = cache.get(logDataIdInRow, () -> fileLogDataStore.getLogData(row));
      logData.setMarked(fileLogDataStore.isMarked(row));
      logData.setMarkerColors(fileLogDataStore.getMarkerColors(row));
      logData.setNote(fileLogDataStore.getNote(row));
      return logData;

    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  public LogData[] getLogData() {
    return fileLogDataStore.getLogData();
  }

  @Override
  public Integer getLogDataIdInRow(int row) {
    return fileLogDataStore.getLogDataIdInRow(row);
  }

  @Override
  public int clear() {
    cache.cleanUp();
    return fileLogDataStore.clear();
  }

  @Override
  public Iterator<LogData> iterator() {
    return fileLogDataStore.iterator();
  }

  public boolean isMarked(int row) {
    return fileLogDataStore.isMarked(row);
  }

  public MarkerColors getMarkerColors(int row) {
    return fileLogDataStore.getMarkerColors(row);
  }

  public void markRows(MarkerColors markerColor, int... rows) {
    fileLogDataStore.markRows(markerColor, rows);
  }

  public void unmarkRows(int... rows) {
    fileLogDataStore.unmarkRows(rows);
  }

  public int getLimit() {
    return fileLogDataStore.getLimit();
  }

  public void setLimit(int limit) {
    fileLogDataStore.setLimit(limit);
  }

  public void addNoteToRow(int row, Note note) {
    fileLogDataStore.addNoteToRow(row, note);
  }

  public Note getNote(int row) {
    return fileLogDataStore.getNote(row);
  }

  public Note removeNote(int row) {
    return fileLogDataStore.removeNote(row);
  }

  public void clearNotes() {
    fileLogDataStore.clearNotes();
  }

  public TreeMap<Integer, Note> getAllNotes() {
    return fileLogDataStore.getAllNotes();
  }

}
