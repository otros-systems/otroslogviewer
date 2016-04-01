package pl.otros.logview.store.async;

import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.model.Note;

import java.util.TreeMap;
import java.util.concurrent.Callable;

public class OperationGetAllNotes implements Callable<TreeMap<Integer, Note>> {


  private final LogDataStore logDataStore;

  public OperationGetAllNotes(LogDataStore logDataStore) {
    this.logDataStore = logDataStore;
  }

  @Override
  public TreeMap<Integer, Note> call() throws Exception {
    return logDataStore.getAllNotes();
  }

}
