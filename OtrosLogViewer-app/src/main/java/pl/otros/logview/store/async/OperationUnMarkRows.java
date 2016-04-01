package pl.otros.logview.store.async;

import pl.otros.logview.api.model.LogDataStore;

import java.util.concurrent.Callable;

public class OperationUnMarkRows implements Callable<Void> {
  private final LogDataStore logDataStore;
  private final int[] rows;

  public OperationUnMarkRows(LogDataStore logDataStore, int... rows) {
    this.logDataStore = logDataStore;
    this.rows = rows;
  }

  @Override
  public Void call() throws Exception {
    logDataStore.unmarkRows(rows);
    return null;
  }
}
