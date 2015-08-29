package pl.otros.logview.store.async;

import pl.otros.logview.store.LogDataStore;

class OperationSetLimit implements Runnable {
  private final LogDataStore logDataStore;
  private final int limit;

  public OperationSetLimit(LogDataStore logDataStore,int limit) {
    this.logDataStore = logDataStore;
    this.limit = limit;
  }

  @Override
  public void run() {
     logDataStore.setLimit(limit);
  }
}
