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

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.parser.JulSimpleFormatterParser;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;
import pl.otros.logview.api.reader.ProxyLogDataCollector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class DetectOnTheFlyLogImporterTest {

  private ArrayList<LogImporter> logImporters;

  @BeforeMethod
public void initialize() throws InitializationException {
    logImporters = new ArrayList<>();
    logImporters.add(new LogImporterUsingParser(new JulSimpleFormatterParser()));
    logImporters.add(new UtilLoggingXmlLogImporter());
    for (LogImporter logImporter : logImporters) {
      logImporter.init(new Properties());
    }

    Log4jPatternMultilineLogParser log4jPatternMultilineLogParser = new Log4jPatternMultilineLogParser();
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
    p.put("name", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("charset", "UTF-8");

    LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(log4jPatternMultilineLogParser);
    logImporterUsingParser.init(p);
    logImporters.add(logImporterUsingParser);

  }

  @Test
  public void testImportLogsStopAddingIfMaxSizeIsReached() {
    // given
    DetectOnTheFlyLogImporter importer = new DetectOnTheFlyLogImporter(logImporters);
    ParsingContext context = new ParsingContext();
    LogDataCollector collector = new ProxyLogDataCollector();
    importer.init(new Properties());
    importer.initParsingContext(context);

    byte[] buff = new byte[importer.detectTryMaximum];

      // when
    importer.importLogs(new ByteArrayInputStream(buff), collector, context);
    importer.importLogs(new ByteArrayInputStream(buff), collector, context);
    // then
    AssertJUnit.assertFalse(context.getCustomConextProperties().containsKey(DetectOnTheFlyLogImporter.PROPERTY_LOG_IMPORTER));
    ByteArrayOutputStream bout = (ByteArrayOutputStream) context.getCustomConextProperties().get(DetectOnTheFlyLogImporter.PROPERTY_BYTE_BUFFER);
    System.out.println("DetectInTheFlyLogImporterTest.testImportLogsStopAddingIfMaxSizeIsReached() " + bout.size());
    AssertJUnit.assertTrue(importer.detectTryMaximum >= bout.size());
  }

  @Test
  public void testImportUtilXml() {
    testImport("julxml/olv0.log", 83);
  }

  @Test
  public void testImportJulSimpleFormatter() {
    testImport("jul_log.txt", 200);

  }

  @Test
  public void testImportLog4jPattern() {
    testImport("log4j.txt", 13);
  }

  @Test
  public void testImportEmptyFile() {
    // given
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    DetectOnTheFlyLogImporter importer = new DetectOnTheFlyLogImporter(logImporters);
    ParsingContext context = new ParsingContext();
    LogDataCollector collector = new ProxyLogDataCollector();
    importer.init(new Properties());
    importer.initParsingContext(context);

    // when
    importer.importLogs(inputStream, collector, context);

    // then
    AssertJUnit.assertNull("Log importer detected", context.getCustomConextProperties().get(DetectOnTheFlyLogImporter.PROPERTY_LOG_IMPORTER));
    AssertJUnit.assertEquals(0, collector.getLogData().length);
  }


 public void testImport(String resourceName, int logDataCount) {
    // given
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
    DetectOnTheFlyLogImporter importer = new DetectOnTheFlyLogImporter(logImporters);
    ParsingContext context = new ParsingContext();
    LogDataCollector collector = new ProxyLogDataCollector();
    importer.init(new Properties());
    importer.initParsingContext(context);

    // when
    importer.importLogs(inputStream, collector, context);

    // then
    AssertJUnit.assertNotNull("Log importer not detected", context.getCustomConextProperties().get(DetectOnTheFlyLogImporter.PROPERTY_LOG_IMPORTER));
    AssertJUnit.assertEquals(logDataCount, collector.getLogData().length);

  }
}
