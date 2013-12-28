package pl.otros.logview.store.async;

import pl.otros.logview.Note;
import pl.otros.logview.store.LogDataStore;

import java.util.concurrent.Callable;

class OperationGetNote implements Callable<Note> {
  private LogDataStore logDataStore;
  private int row;

  public OperationGetNote(LogDataStore logDataStore, int row) {
    this.logDataStore = logDataStore;
    this.row = row;
  }

  @Override
  public Note call() {
     return logDataStore.getNote(row);
  }
}
