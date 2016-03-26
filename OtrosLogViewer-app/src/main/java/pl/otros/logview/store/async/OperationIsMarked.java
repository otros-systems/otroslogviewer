package pl.otros.logview.store.async;

import pl.otros.logview.api.model.LogDataStore;

import java.util.concurrent.Callable;

class OperationIsMarked implements Callable<Boolean> {
  private final LogDataStore logDataStore;
  private final int row;

  public OperationIsMarked(LogDataStore logDataStore, int row) {
    this.logDataStore = logDataStore;
    this.row = row;
  }

  @Override
  public Boolean call() {
     return logDataStore.isMarked(row);
  }
}
