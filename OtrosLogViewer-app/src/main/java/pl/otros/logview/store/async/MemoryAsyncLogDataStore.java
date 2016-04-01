package pl.otros.logview.store.async;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;
import pl.otros.logview.gui.actions.search.SearchResult;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class MemoryAsyncLogDataStore implements AsyncLogDataStore {

  private final LogDataStore logDataStore;
  private final ListeningExecutorService service;

  public MemoryAsyncLogDataStore(ListeningExecutorService service, LogDataStore logDataStore) {
    this.service = service;


    this.logDataStore = logDataStore;
  }

  @Override
  public int getCount() {
    try {
      return service.submit(new OperationGetCount(logDataStore)).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public ListenableFuture remove(final int... ids) {
    ListenableFuture<?> submit = service.submit(new OperationRemove(logDataStore, ids));
    return submit;
  }

  @Override
  public ListenableFuture<FilterResult> filter(LogDataFilter filter) {
    //TODO
    return null;
  }

  @Override
  public ListenableFuture<SearchResult> search(SearchCriteria searchCriteria) {
    //TODO
    return null;
  }

  @Override
  public LogData getLogData(int row) {
    return runCallableInDedicatedThread(new OperationGetLogData(logDataStore, row));
  }

  @Override
  public Integer getLogDataIdInRow(int row) {
    return runCallableInDedicatedThread(new OperationGetDataIdInRow(logDataStore, row));
  }

  @Override
  public int getLimit() {
    try {
      return service.submit(new OperationGetLimit(logDataStore)).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public void setLimit(int limit) {
    service.submit(new OperationSetLimit(logDataStore, limit));
  }

  @Override
  public void add(final LogData... logDatas) {
    service.submit(() -> logDataStore.add(logDatas));
  }

  @Override
  public LogData[] getLogData() {
    return logDataStore.getLogData();
  }


  @Override
  public int clear() {
    try {
      return service.submit(new OperationClear(logDataStore)).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public void addNoteToRow(int row, Note note) {
    runCallableInDedicatedThread(new OpeartionAddNoteToRow(logDataStore, row, note));
  }

  @Override
  public Note getNote(int row) {
    return runCallableInDedicatedThread(new OperationGetNote(logDataStore, row));
  }

  @Override
  public Note removeNote(int row) {
    return runCallableInDedicatedThread(new OperationRemoveNote(logDataStore, row));
  }


  @Override
  public void clearNotes() {
    try {
      service.submit(new OperationClearNotes(logDataStore)).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

  }

  @Override
  public TreeMap<Integer, Note> getAllNotes() {
    Callable<TreeMap<Integer, Note>> task = new OperationGetAllNotes(logDataStore);
    return runCallableInDedicatedThread(task);
  }

  private <T> T runCallableInDedicatedThread(Callable<T> task) {
    try {
      T t = service.submit(task).get();
      return t;
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Iterator<LogData> iterator() {
    try {
      return service.submit(new OperationGetIterator(logDataStore)).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean isMarked(int row) {
    //TODO filter support
    try {
      return service.submit(new OperationIsMarked(logDataStore, row)).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public MarkerColors getMarkerColors(int row) {
    //TODO filter support
    try {
      return service.submit(new OperationGetMarkerColors(logDataStore, row)).get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void markRows(MarkerColors markerColors, int... rows) {
    runCallableInDedicatedThread(new OperationMarkRows(logDataStore, markerColors, rows));
  }

  @Override
  public void unmarkRows(int... rows) {
    runCallableInDedicatedThread(new OperationUnMarkRows(logDataStore, rows));

  }

}
