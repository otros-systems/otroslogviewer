package pl.otros.logview.store.async;

import pl.otros.logview.api.Note;
import pl.otros.logview.api.LogDataStore;

import java.util.concurrent.Callable;

class OperationGetNote implements Callable<Note> {
  private final LogDataStore logDataStore;
  private final int row;

  public OperationGetNote(LogDataStore logDataStore, int row) {
    this.logDataStore = logDataStore;
    this.row = row;
  }

  @Override
  public Note call() {
     return logDataStore.getNote(row);
  }
}
