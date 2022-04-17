package pl.otros.logview.store.async;

import pl.otros.logview.api.model.LogDataStore;
import pl.otros.logview.api.model.Note;

import java.util.concurrent.Callable;

class OperationRemoveNote implements Callable<Note> {
  private final LogDataStore logDataStore;
  private final int row;

  public OperationRemoveNote(LogDataStore logDataStore, int row) {
    this.logDataStore = logDataStore;
    this.row = row;
  }

  @Override
  public Note call() {
    return logDataStore.removeNote(row);
  }
}
