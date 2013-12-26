package pl.otros.logview.store.async;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import pl.otros.logview.LogData;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.gui.actions.search.SearchResult;
import pl.otros.logview.store.LogDataStore;
import pl.otros.logview.store.MemoryLogDataStore;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class MemoryAsyncLogDataStore implements AsyncLogDataStore {

  private LogDataStore logDataStore;
  private ListeningExecutorService service;

  public MemoryAsyncLogDataStore(ListeningExecutorService service, LogDataStore logDataStore) {
    this.service = service;


    this.logDataStore = logDataStore;
  }

  @Override
  public int getCount() {
    try {
      return service.submit(new OperationGetCount(logDataStore)).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
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
    try {
      return service.submit(new OperationGetLogData(logDataStore, row)).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Integer getLogDataIdInRow(int row) {
    return logDataStore.getLogDataIdInRow(row);
  }

  @Override
  public int getLimit() {
    try {
      return service.submit(new OperationGetLimit(logDataStore)).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
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
    service.submit(new Runnable() {
      @Override
      public void run() {
        logDataStore.add(logDatas);

      }
    });
  }

  @Override
  public LogData[] getLogData() {
    return logDataStore.getLogData();
  }


  @Override
  public int clear() {
    try {
      return service.submit(new OperationClear(logDataStore)).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public void addNoteToRow(int row, Note note) {
    logDataStore.addNoteToRow(row, note);
  }

  @Override
  public Note getNote(int row) {
    return logDataStore.getNote(row);
  }

  @Override
  public Note removeNote(int row) {
    return logDataStore.removeNote(row);
  }


  @Override
  public void clearNotes() {
    logDataStore.clearNotes();

  }

  @Override
  public TreeMap<Integer, Note> getAllNotes() {
    return logDataStore.getAllNotes();
  }

  @Override
  public Iterator<LogData> iterator() {
    return logDataStore.iterator();
  }

  @Override
  public boolean isMarked(int row) {
    //TODO filter support
    return logDataStore.isMarked(row);
  }

  @Override
  public MarkerColors getMarkerColors(int row) {
    //TODO filter support
    return logDataStore.getMarkerColors(row);
  }

  @Override
  public void markRows(MarkerColors markerColors, int... rows) {
    logDataStore.markRows(markerColors, rows);
  }

  @Override
  public void unmarkRows(int... rows) {
    logDataStore.unmarkRows(rows);

  }

}
