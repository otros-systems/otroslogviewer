package pl.otros.logview.store.async;

import pl.otros.logview.store.LogDataStore;

import java.util.concurrent.Callable;

class OperationGetLimit implements Callable<Integer> {
  private LogDataStore logDataStore;

  public OperationGetLimit(LogDataStore logDataStore) {
    this.logDataStore = logDataStore;
  }

  @Override
  public Integer call() {
     return logDataStore.getLimit();
  }
}
