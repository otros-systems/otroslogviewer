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
package pl.otros.logview;

import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.parser.ParsingContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;

public class TestUtils {

  private static final Level[] LEVELS = { Level.FINE, Level.FINER, Level.FINEST, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE };

  private static final String LOGGERS[] = { "com.test.class1", "com.test.class2", "com.test2.class3", "com.test2.class1", "com.test2.class1",
      "com.ad.cd", };

  private static final String THREADS[] = { "deamon", "connection watcher", "main", "socket listener 1", "socket listener 2", };

  private static final String METHODS[] = { "met2", "m34", "main", "superMethod", "wow", };

  private static final Random RANDOM = new Random(System.currentTimeMillis());
  private static int id = 0;

  public static LogData generateLogData() {
    LogData ld = new LogData();
    ld.setDate(new Date());
    ld.setLevel(LEVELS[RANDOM.nextInt(LEVELS.length)]);
    ld.setLoggerName(LOGGERS[RANDOM.nextInt(LOGGERS.length)]);
    ld.setClazz(LOGGERS[RANDOM.nextInt(LOGGERS.length)]);
    ld.setThread(THREADS[RANDOM.nextInt(THREADS.length)]);
    ld.setMethod(METHODS[RANDOM.nextInt(METHODS.length)]);
    ld.setId(id++);
    ld.setMessage("message sfd");
    return ld;
  }

  public static void tailLog(LogImporter importer, InputStream in, LogDataCollector collector, ParsingContext parsingContext) throws IOException{
		byte[] buff = new byte[10*1024];
		int read = 0;
		while ((read = in.read(buff))>0){
			ByteArrayInputStream bin = new ByteArrayInputStream(buff,0,read);
			importer.importLogs(bin, collector, parsingContext);
		}
	}
  
  @Test
  public void doNothing() {
  }
}
