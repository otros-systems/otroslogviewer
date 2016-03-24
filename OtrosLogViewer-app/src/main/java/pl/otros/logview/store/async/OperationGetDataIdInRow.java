package pl.otros.logview.store.async;

import pl.otros.logview.api.LogDataStore;

import java.util.concurrent.Callable;

public class OperationGetDataIdInRow implements Callable<Integer> {
  private final LogDataStore logDataStore;
  private final int row;

  public OperationGetDataIdInRow(LogDataStore logDataStore, int row) {
    this.logDataStore = logDataStore;
    this.row = row;
  }

  @Override
  public Integer call() throws Exception {
    return logDataStore.getLogDataIdInRow(row);
  }
}
