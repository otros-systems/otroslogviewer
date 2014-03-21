/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
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

package pl.otros.logview.importer;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import pl.otros.logview.LogData;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.reader.ProxyLogDataCollector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;

public class Log4jSerilizedLogImporterTest {

  @Test
  public void testImportLogs() throws IOException, InitializationException {
    // given
    LoggingEvent[] sourceEvents = new LoggingEvent[5];
    for (int i = 0; i < sourceEvents.length; i++) {
      Level info = Level.INFO;
      String message = Integer.toString(i);
      long timestamp = i;
      Logger logger = Logger.getLogger("MyLogger");

      LoggingEvent event = new LoggingEvent(null, logger, timestamp, info, message, null);

      sourceEvents[i] = event;
    }

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream oout = new ObjectOutputStream(bout);
    for (LoggingEvent loggingEvent : sourceEvents) {
      oout.writeObject(loggingEvent);
    }
    oout.flush();

    Log4jSerilizedLogImporter importer = new Log4jSerilizedLogImporter();
    importer.init(new Properties());
    ParsingContext parsingContext = new ParsingContext();
    importer.initParsingContext(parsingContext);
    ProxyLogDataCollector collector = new ProxyLogDataCollector();

    // when
    importer.importLogs(new ByteArrayInputStream(bout.toByteArray()), collector, parsingContext);

    // then
    LogData[] parsedLogData = collector.getLogData();
    assertEquals(sourceEvents.length, parsedLogData.length);
    for (int i = 0; i < parsedLogData.length; i++) {
      assertEquals(sourceEvents[i].getTimeStamp(), parsedLogData[i].getDate().getTime());
      assertEquals(sourceEvents[i].getMessage(), parsedLogData[i].getMessage());
    }

  }

}
