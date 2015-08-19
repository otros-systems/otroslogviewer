package pl.otros.logview.store.async;

import pl.otros.logview.LogData;
import pl.otros.logview.store.LogDataStore;

import java.util.Iterator;
import java.util.concurrent.Callable;

class OperationGetIterator implements Callable<Iterator<LogData>> {
  private final LogDataStore logDataStore;

  public OperationGetIterator(LogDataStore logDataStore) {
    this.logDataStore = logDataStore;
  }

  @Override
  public Iterator<LogData> call() {
     return logDataStore.iterator();
  }
}
