package pl.otros.logview.store.async;

import pl.otros.logview.store.LogDataStore;

class OperationRemove implements Runnable {
  private final int[] ids;
  private LogDataStore logDataStore;

  public OperationRemove(LogDataStore logDataStore, int... ids) {
    this.logDataStore = logDataStore;
    this.ids = ids;
  }

  @Override
  public void run() {
     logDataStore.remove(ids);
  }
}
