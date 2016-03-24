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
package pl.otros.logview.parser.log4j;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.LogImporterUsingParser;
import pl.otros.logview.api.ParsingContext;
import pl.otros.logview.api.reader.ProxyLogDataCollector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;

import static org.testng.AssertJUnit.assertEquals;

public class Log4jPatternMultilineLogParserTest {

  @Test
  public void testCustomLevel() throws IOException, InitializationException {
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
    p.put("customLevels", "L1=TRACE,L2=DEBUG,L3=INFO,L4=WARN,L5=ERROR");
    Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
    InputStream in = loadLog("log4j/log4j_cusom_level.txt");
    LogImporterUsingParser importerUsingParser = new LogImporterUsingParser(logParser);
    ParsingContext context = new ParsingContext("?", "log4j/log4j_cusom_level.txt");
    importerUsingParser.init(p);
    importerUsingParser.initParsingContext(context);

    // when
    ProxyLogDataCollector dataCollector = new ProxyLogDataCollector();
    importerUsingParser.importLogs(in, dataCollector, context);

    // then
    LogData[] logDatas = dataCollector.getLogData();
    assertEquals(10, logDatas.length);
    assertEquals(Level.FINEST, logDatas[0].getLevel());
    assertEquals(Level.FINE, logDatas[1].getLevel());
    assertEquals(Level.INFO, logDatas[2].getLevel());
    assertEquals(Level.WARNING, logDatas[3].getLevel());
    assertEquals(Level.SEVERE, logDatas[4].getLevel());
    assertEquals(Level.FINEST, logDatas[5].getLevel());
    assertEquals(Level.FINE, logDatas[6].getLevel());
    assertEquals(Level.INFO, logDatas[7].getLevel());
    assertEquals(Level.WARNING, logDatas[8].getLevel());
    assertEquals(Level.SEVERE, logDatas[9].getLevel());

    for (int i = 0; i < logDatas.length; i++) {
      assertEquals(String.format("LogData source for event %d of %d", i, logDatas.length), context.getLogSource(), logDatas[i].getLogSource());
    }

  }

  @Test
  public void testDefaultCharset() throws InitializationException {
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
    // p.put("name", "windows-1250");
    Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
    logParser.init(p);

    assertEquals("UTF-8", logParser.getParserDescription().getCharset());
  }

  @Test
  public void testCustomCharset() throws Throwable {
    // given
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
    p.put("name", "windows-1250");
    p.put("charset", "windows-1250");
    InputStream in = loadLog("log4j/log4j_pl.txt");
    Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
    LogImporterUsingParser importerUsingParser = new LogImporterUsingParser(logParser);
    importerUsingParser.init(p);
    ParsingContext context = new ParsingContext();
    importerUsingParser.initParsingContext(context);

    // when
    ProxyLogDataCollector dataCollector = new ProxyLogDataCollector();
    importerUsingParser.importLogs(in, dataCollector, context);

    // then

    assertEquals("windows-1250", logParser.getParserDescription().getCharset());
    LogData[] logDatas = dataCollector.getLogData();
    assertEquals(6, logDatas.length);
    assertEquals('a', logDatas[2].getMessage().toCharArray()[0]);
    assertEquals(261, logDatas[2].getMessage().toCharArray()[1]);

    assertEquals('e', logDatas[3].getMessage().toCharArray()[0]);
    assertEquals(281, logDatas[3].getMessage().toCharArray()[1]);

    assertEquals('z', logDatas[4].getMessage().toCharArray()[0]);
    assertEquals(380, logDatas[4].getMessage().toCharArray()[1]);

    assertEquals('l', logDatas[5].getMessage().toCharArray()[0]);
    assertEquals(322, logDatas[5].getMessage().toCharArray()[1]);
  }

  @Test
  public void testLevelWithSpaces() throws IOException, InitializationException {
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP|LEVEL|CLASS|MESSAGE");
    p.put("dateFormat", "yyyy.MM.dd HH:mm:ss.SSS");
    Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
    InputStream in = loadLog("log4j/log4j_pattern_level_with_spaces.log");
    LogImporterUsingParser importerUsingParser = new LogImporterUsingParser(logParser);
    ParsingContext context = new ParsingContext();
    importerUsingParser.init(p);
    importerUsingParser.initParsingContext(context);

    // when
    ProxyLogDataCollector dataCollector = new ProxyLogDataCollector();
    importerUsingParser.importLogs(in, dataCollector, context);

    // then
    LogData[] logDatas = dataCollector.getLogData();
    assertEquals(9, logDatas.length);
    assertEquals(Level.FINE, logDatas[0].getLevel());
    assertEquals(Level.INFO, logDatas[1].getLevel());
    assertEquals(Level.FINE, logDatas[2].getLevel());
    assertEquals(Level.WARNING, logDatas[3].getLevel());
    assertEquals(Level.FINE, logDatas[4].getLevel());
    assertEquals(Level.INFO, logDatas[5].getLevel());
    assertEquals(Level.FINE, logDatas[6].getLevel());
    assertEquals(Level.SEVERE, logDatas[7].getLevel());
    assertEquals(Level.FINE, logDatas[8].getLevel());

  }

  @Test
  public void testDatePatternWithT() throws IOException, InitializationException, ParseException {
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP | PROP(seq) | THREAD | PROP(thrN) | LEVEL | CLASS | MESSAGE");
    p.put("dateFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    Log4jPatternMultilineLogParser logParser = new Log4jPatternMultilineLogParser();
    InputStream in = loadLog("log4j/log4j_date_pattern_with_T.txt");
    LogImporterUsingParser importerUsingParser = new LogImporterUsingParser(logParser);
    ParsingContext context = new ParsingContext();
    importerUsingParser.init(p);
    importerUsingParser.initParsingContext(context);

    // when
    ProxyLogDataCollector dataCollector = new ProxyLogDataCollector();
    importerUsingParser.importLogs(in, dataCollector, context);

    // then
    LogData[] logDatas = dataCollector.getLogData();
    assertEquals(2, logDatas.length);
    assertEquals(Level.INFO, logDatas[0].getLevel());
    assertEquals("ENTRY something", logDatas[0].getMessage());
    assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").parse("2012-01-12 20:02:00.237 +0200").getTime(), logDatas[0].getDate().getTime());
    assertEquals("00002065", logDatas[0].getThread());
    assertEquals("MyClass", logDatas[0].getClazz());
    assertEquals("(8,617,481)", logDatas[0].getProperties().get("seq"));
    assertEquals("ThrN1", logDatas[0].getProperties().get("thrN"));

    assertEquals(Level.INFO, logDatas[1].getLevel());
    assertEquals("msg", logDatas[1].getMessage());
    assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").parse("2012-01-12 20:03:00.240 +0200").getTime(), logDatas[1].getDate().getTime());
    assertEquals("00002065", logDatas[1].getThread());
    assertEquals("MyClass", logDatas[1].getClazz());
    assertEquals("(8,617,482)", logDatas[1].getProperties().get("seq"));
    assertEquals("Thrn2", logDatas[1].getProperties().get("thrN"));

  }

  private InputStream loadLog(String resource) throws IOException {
    return new ByteArrayInputStream(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(resource)));
  }

}
