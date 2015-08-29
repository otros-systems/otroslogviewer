package pl.otros.logview.store.async;

import pl.otros.logview.LogData;
import pl.otros.logview.store.LogDataStore;

import java.util.concurrent.Callable;

class OperationGetLogData implements Callable<LogData> {
  private final LogDataStore logDataStore;
  private final int id;

  public OperationGetLogData(LogDataStore logDataStore, int id) {
    this.logDataStore = logDataStore;
    this.id = id;
  }

  @Override
  public LogData call() {
     return logDataStore.getLogData(id);
  }
}
