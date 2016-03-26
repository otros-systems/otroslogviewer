/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package examples;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.vfs2.FileObject;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.batch.BatchProcessingContext;
import pl.otros.logview.api.batch.BatchProcessingListener;
import pl.otros.logview.api.batch.LogDataParsedListener;
import pl.otros.logview.api.batch.SingleFileBatchProcessingListener;

import java.util.Iterator;
import java.util.logging.Level;

public class ExampleLogDataParsedListener implements BatchProcessingListener, SingleFileBatchProcessingListener, LogDataParsedListener {

  int totalCount = 0;
  int singleFileCount = 0;

  @Override
  public void logDataParsed(LogData logData, BatchProcessingContext context) {
    if (logData.getLevel() != null && logData.getLevel().intValue() >= Level.WARNING.intValue()) {
      System.out.printf("Event with level %s at %2$tH:%2$tM:%2$tS %2$te-%2$tm-%2$tY: %3$s\n", logData.getLevel(), logData.getDate(), logData.getMessage());
      totalCount++;
      singleFileCount++;
    }
  }

  @Override
  public void processingStarted(BatchProcessingContext batchProcessingContext) {
    System.out.println("Batch processing started");
    DataConfiguration configuration = batchProcessingContext.getConfiguration();
    System.out.println("Dumping batch processing configuration.");
    Iterator<String> keys = configuration.getKeys();
    while (keys.hasNext()) {
      String next = keys.next();
      System.out.printf("%s=%s%n", next, configuration.getString(next));
    }
  }

  @Override
  public void processingFinished(BatchProcessingContext batchProcessingContext) {
    System.out.printf("Finished parsing all files, found %d event at level warning or higher\n", totalCount);
  }

  @Override
  public void processingFileStarted(BatchProcessingContext batchProcessingContext) {
    String baseName = batchProcessingContext.getCurrentFile().getName().getBaseName();
    System.out.printf("Processing file %s have started\n", baseName);
    singleFileCount = 0;
  }

  @Override
  public void processingFileFinished(BatchProcessingContext batchProcessingContext) {
    FileObject currentFile = batchProcessingContext.getCurrentFile();
    System.out.printf("Finished parsing file %s, found %d event at level warning or higher\n", currentFile.getName().getBaseName(), singleFileCount);
  }

}
