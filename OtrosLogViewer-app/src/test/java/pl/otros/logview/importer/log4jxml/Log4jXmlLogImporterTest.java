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

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.util.logging.Level.INFO;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static pl.otros.logview.RenamedLevel.DEBUG;
import static pl.otros.logview.RenamedLevel.ERROR;
import static pl.otros.logview.RenamedLevel.FATAL;
import static pl.otros.logview.RenamedLevel.TRACE;
import static pl.otros.logview.RenamedLevel.WARN;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.testng.annotations.Test;

import pl.otros.logview.TestUtils;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.reader.ProxyLogDataCollector;

public class Log4jXmlLogImporterTest {

  @Test
  public void testImportDefaultLog() throws Exception {
    testImportLogs("log4j/log4j_default.xml");
  }

  @Test
  public void testImportMDCWithChildNodes() throws Exception {
    testImportLogs("log4j/log4j_MDC_childnodes.xml");
  }

  @Test
  public void testImportMDCWithKeyValue() throws Exception {
    testImportLogs("log4j/log4j_MDC_key_value.xml");
  }

  @Test
  public void testImportNestedCData() throws Exception {
    testImportLogs("log4j/log4j_nested_cdata.xml");
  }

  @Test
  public void testImportInvalidCharacters() throws Exception {
    testImportLogs("log4j/log4j_invalid_chars.xml");
  }

  private static void testImportLogs(String resourceName) throws Exception {
    // given
    URL resource = getSystemResource(resourceName);
    ProxyLogDataCollector collector = new ProxyLogDataCollector();
    ParsingContext parsingContext = new ParsingContext(resourceName, resource.toString());
    Log4jXmlLogImporter importer = new Log4jXmlLogImporter();
    importer.init(new Properties());
    importer.initParsingContext(parsingContext);

    // when
    try (InputStream in = resource.openStream()) {
      importer.importLogs(in, collector, parsingContext);
    }

    // then
    LogData[] logDatas = collector.getLogData();
    assertEquals("Logs loaded", 6, logDatas.length);
    assertLogData(logDatas[0], TRACE, parsingContext);
    assertLogData(logDatas[1], DEBUG, parsingContext);
    assertLogData(logDatas[2], INFO, parsingContext);
    assertLogData(logDatas[3], WARN, parsingContext);
    assertLogData(logDatas[4], ERROR, parsingContext);
    assertLogData(logDatas[5], FATAL, parsingContext);
  }

  private static void assertLogData(LogData logData, Level expectedLevel, ParsingContext parsingContext) throws Exception {
    String message = removeCDataTags(logData.getMessage()); // remove for the sake of common assertions
    String ndc = removeCDataTags(logData.getNDC());
    
    assertEquals("Classname", logData.getLoggerName());
    assertEquals(new Date(1483646202027l), logData.getDate());
    assertEquals(expectedLevel, logData.getLevel());
    assertEquals("main", logData.getThread());
    assertEquals("A descriptive message\uFFFD\njava.lang.Exception: A nice exception\n\tat Classname.main(Classname.java:18)", message);
    assertEquals("Outer section Inner section", ndc);
    assertEquals("Classname", logData.getClazz());
    assertEquals("main", logData.getMethod());
    assertEquals("18", logData.getLine());

    Map<String, String> expectedProperties = new HashMap<>();
    expectedProperties.put("correlationID", "0050569c-5ef2-11e5-e246-876af0931fc5");
    expectedProperties.put("username", "cn=Directory Manager\uFFFD,o=mycompany.com");

    assertEquals(expectedProperties, logData.getProperties());
    assertEquals(parsingContext.getLogSource(), logData.getLogSource());
  }
  
  private static String removeCDataTags(String string){
    return string.replace("<![CDATA[", "").replace("]]>", "");
  }

  @Test
  public void testITailLogs() throws InitializationException, IOException {
    // given
    InputStream in = getSystemResourceAsStream("log4j/log4jBig.xml.gz");
    in = new GZIPInputStream(in);
    assertNotNull("Log input strem from log4j.xml", in);
    ProxyLogDataCollector collector = new ProxyLogDataCollector();
    ParsingContext parsingContext = new ParsingContext("?", "classpath://log4j/log4jBig.xml.gz");

    Log4jXmlLogImporter importer = new Log4jXmlLogImporter();
    importer.init(new Properties());
    importer.initParsingContext(parsingContext);

    // when
    TestUtils.tailLog(importer, in, collector, parsingContext);
    // importer.importLogs(in, collector, parsingContext);

    // then
    LogData[] logDatas = collector.getLogData();
    assertEquals("Logs loaded", 10000, logDatas.length);
    assertEquals(logDatas[0].getLevel(), INFO);
    assertEquals(logDatas[0].getLogSource(), parsingContext.getLogSource());
  }
}
