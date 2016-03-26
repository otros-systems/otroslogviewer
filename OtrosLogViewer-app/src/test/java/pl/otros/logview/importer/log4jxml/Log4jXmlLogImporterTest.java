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
package pl.otros.logview.importer.log4jxml;

import org.testng.annotations.Test;
import pl.otros.logview.TestUtils;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.reader.ProxyLogDataCollector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class Log4jXmlLogImporterTest {

  @Test
  public void testImportLogs() throws InitializationException {
    // given
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("log4jXml.log");
    // this.getClass().getClassLoader().getResourceAsStream(resName);
    assertNotNull("Log input strem from log4j.xml", in);
    ProxyLogDataCollector collector = new ProxyLogDataCollector();
    ParsingContext parsingContext = new ParsingContext("?", "classpath://log4jXml.log");
    Log4jXmlLogImporter importer = new Log4jXmlLogImporter();    
    importer.init(new Properties());
    importer.initParsingContext(parsingContext);

    // when
    long d = System.currentTimeMillis();
    importer.importLogs(in, collector, parsingContext);
    System.out.println(System.currentTimeMillis()-d + "ms");

    // then
    LogData[] logDatas = collector.getLogData();
    assertEquals("Logs loaded", 6, logDatas.length);
    assertEquals(logDatas[0].getLevel(), Level.INFO);
    assertEquals(logDatas[0].getMessage(), "Info level");

    assertEquals(logDatas[2].getLevel(), Level.WARNING);
    assertEquals(logDatas[2].getMessage(), "warn level");

    for (LogData logData : logDatas) {
      assertEquals(logData.getLogSource(), parsingContext.getLogSource());
    }

  }
  
  @Test
  public void testITailLogs() throws InitializationException, IOException {
    // given
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("log4j/log4jBig.xml.gz");
    in = new GZIPInputStream(in);
    // this.getClass().getClassLoader().getResourceAsStream(resName);
    assertNotNull("Log input strem from log4j.xml", in);
    ProxyLogDataCollector collector = new ProxyLogDataCollector();
    ParsingContext parsingContext = new ParsingContext("?", "classpath://log4j/log4jBig.xml.gz");

    Log4jXmlLogImporter importer = new Log4jXmlLogImporter();
    importer.init(new Properties());
    importer.initParsingContext(parsingContext);

    // when
    TestUtils.tailLog(importer, in, collector, parsingContext);
//    importer.importLogs(in, collector, parsingContext);

    // then
    LogData[] logDatas = collector.getLogData();
    assertEquals("Logs loaded", 10000, logDatas.length);
    assertEquals(logDatas[0].getLevel(), Level.INFO);
    assertEquals(logDatas[0].getLogSource(), parsingContext.getLogSource());

  }

}
