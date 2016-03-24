package pl.otros.logview.store.async;

import pl.otros.logview.api.MarkerColors;
import pl.otros.logview.api.LogDataStore;

import java.util.concurrent.Callable;

public class OperationMarkRows implements Callable<Void> {
  private final LogDataStore logDataStore;
  private final MarkerColors markerColors;
  private final int[] rows;

  public OperationMarkRows(LogDataStore logDataStore, MarkerColors markerColors, int... rows) {
    this.logDataStore = logDataStore;
    this.markerColors = markerColors;
    this.rows = rows;
  }

  @Override
  public Void call() throws Exception {
    logDataStore.markRows(markerColors,rows);
    return null;
  }
}
