package pl.otros.logview.store.async;

import pl.otros.logview.store.LogDataStore;

class OperationClearNotes implements Runnable {
  private final LogDataStore logDataStore;

  public OperationClearNotes(LogDataStore logDataStore) {
    this.logDataStore = logDataStore;
  }

  @Override
  public void run() {
     logDataStore.clearNotes();
  }
}
