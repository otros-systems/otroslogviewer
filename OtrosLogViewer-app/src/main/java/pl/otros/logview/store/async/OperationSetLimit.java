package pl.otros.logview.store.async;

import pl.otros.logview.store.LogDataStore;

import java.util.concurrent.Callable;

class OperationSetLimit implements Runnable {
  private LogDataStore logDataStore;
  private int limit;

  public OperationSetLimit(LogDataStore logDataStore,int limit) {
    this.logDataStore = logDataStore;
    this.limit = limit;
  }

  @Override
  public void run() {
     logDataStore.setLimit(limit);
  }
}
