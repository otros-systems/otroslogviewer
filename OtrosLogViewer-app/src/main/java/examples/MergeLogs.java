package examples;

import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.batch.BatchProcessingContext;
import pl.otros.logview.api.batch.BatchProcessingListener;
import pl.otros.logview.api.batch.LogDataParsedListener;

public class MergeLogs implements BatchProcessingListener, LogDataParsedListener {

  @Override
  public void processingStarted(BatchProcessingContext batchProcessingContext) throws Exception {

  }

  @Override
  public void processingFinished(BatchProcessingContext batchProcessingContext) throws Exception {
    batchProcessingContext.saveLogDataStore("mergedLogs","Merged logs");
  }

  @Override
  public void logDataParsed(LogData data, BatchProcessingContext context) throws Exception {
    context.getDataStore().add(data);
  }


}
