package pl.otros.logview.store.async;

import pl.otros.logview.Note;
import pl.otros.logview.store.LogDataStore;

import java.util.concurrent.Callable;

class OperationRemoveNote implements Callable<Note> {
  private LogDataStore logDataStore;
  private int row;

  public OperationRemoveNote(LogDataStore logDataStore, int row) {
    this.logDataStore = logDataStore;
    this.row = row;
  }

  @Override
  public Note call() {
    return logDataStore.removeNote(row);
  }
}
