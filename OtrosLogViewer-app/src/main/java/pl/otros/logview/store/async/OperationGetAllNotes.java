package pl.otros.logview.store.async;

import pl.otros.logview.Note;
import pl.otros.logview.store.LogDataStore;

import java.util.TreeMap;
import java.util.concurrent.Callable;

public class OperationGetAllNotes implements Callable<TreeMap<Integer,Note>> {


  private final LogDataStore logDataStore;

  public OperationGetAllNotes(LogDataStore logDataStore) {
    this.logDataStore = logDataStore;
  }

  @Override
  public TreeMap<Integer,Note> call() throws Exception {
    return logDataStore.getAllNotes();
  }

}
