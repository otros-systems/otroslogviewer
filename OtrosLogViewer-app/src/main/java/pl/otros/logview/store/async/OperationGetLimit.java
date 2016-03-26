package pl.otros.logview.store.async;

import pl.otros.logview.api.model.LogDataStore;

import java.util.concurrent.Callable;

class OperationGetLimit implements Callable<Integer> {
  private final LogDataStore logDataStore;

  public OperationGetLimit(LogDataStore logDataStore) {
    this.logDataStore = logDataStore;
  }

  @Override
  public Integer call() {
     return logDataStore.getLimit();
  }
}
