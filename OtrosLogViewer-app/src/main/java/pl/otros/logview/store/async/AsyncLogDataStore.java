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
package pl.otros.logview.store.async;

import com.google.common.util.concurrent.ListenableFuture;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataCollector;
import pl.otros.logview.Note;
import pl.otros.logview.gui.MarkableTableModel;
import pl.otros.logview.gui.actions.search.SearchResult;

import java.util.TreeMap;

public interface AsyncLogDataStore extends MarkableTableModel, Iterable<LogData>, LogDataCollector {

  public int getCount();

  @Async
  public ListenableFuture remove(int... ids);

  @Async
  public ListenableFuture<FilterResult> filter(LogDataFilter filter);

  @Async
  public ListenableFuture<SearchResult> search(SearchCriteria searchCriteria);

  public LogData getLogData(int row);

  public Integer getLogDataIdInRow(int row);

  public int getLimit();

  public void setLimit(int limit);

  public int clear();

  public void addNoteToRow(int row, Note note);

  public Note getNote(int row);

  public Note removeNote(int row);

  public void clearNotes();

  public TreeMap<Integer, Note> getAllNotes();

}
