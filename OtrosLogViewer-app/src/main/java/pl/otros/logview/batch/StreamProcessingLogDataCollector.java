/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.batch.BatchProcessingContext;
import pl.otros.logview.api.batch.LogDataParsedListener;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;

public class StreamProcessingLogDataCollector implements LogDataCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamProcessingLogDataCollector.class.getName());
  private final LogDataParsedListener logDataParsedListener;
  private final BatchProcessingContext batchProcessingContext;


  public StreamProcessingLogDataCollector(LogDataParsedListener logDataParsedListener, BatchProcessingContext batchProcessingContext) {
    super();
    this.logDataParsedListener = logDataParsedListener;
    this.batchProcessingContext = batchProcessingContext;
  }

  @Override
  public void add(LogData... logDatas) {
    for (int i = 0; i < logDatas.length; i++) {
      try {
        logDataParsedListener.logDataParsed(logDatas[i], batchProcessingContext);
      } catch (Exception e) {
        LOGGER.error("Error processing log data " + e.getMessage(), e);
        System.out.println("Error processing log data " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public LogData[] getLogData() {
    return new LogData[0];
  }

  @Override
  public int clear() {
    return 0;
  }

}
