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

import pl.otros.logview.LogData;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.parser.MultiLineLogParser;
import pl.otros.logview.parser.ParserDescription;
import pl.otros.logview.parser.ParsingContext;

import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log4jPatternIsoDate5pTMNLogParser implements MultiLineLogParser {

  private static final Logger LOGGER = Logger.getLogger(Log4jPatternIsoDate5pTMNLogParser.class.getName());

  private ParserDescription pd;

  // private SimpleDateFormat datePattern = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.ENGLISH);

  public Log4jPatternIsoDate5pTMNLogParser() {
    LOGGER.fine("Initializing");
    pd = new ParserDescription();
    pd.setDisplayName("Log4j: \"%d{ISO8601} %-5p [%t] %m%n\"");
    pd.setDescription("Parse logs formmated by log4j pattern \"%d{ISO8601} %-5p [%t] %m%n\"");
    pd.setKeyStrokeAccelelator("control j");
    pd.setMenmonic(KeyEvent.VK_G);
  }

  @Override
  public LogData parse(String event, ParsingContext context) throws ParseException {
    LOGGER.finest("Parsing event: \"" + event + "\n");
    if (event == null || event.length() == 0) {
      LOGGER.finest("Event null or 0 size, returning null");
      return null;
    }
    StringBuilder sb = context.getUnmatchedLog();

    try {
      boolean dateFound = tryToFindDate(event, context.getDateFormat());
      LOGGER.finest("Trying to find date in event: " + dateFound);
      if (!dateFound) {
        LOGGER.finest("date not fount, return null");
        return null;
      }
      return tryToParseStringBuilder(sb, context.getDateFormat());
    } catch (IndexOutOfBoundsException e) {
      return null;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return null;
    } catch (Exception e) {
      System.err.println("Exception: " + e.getClass());
      System.err.println("Exception " + e.getMessage());
      System.err.println("event: " + event);
      System.err.println("Buffer: " + sb.toString());
      return null;
    } finally {
      sb.append(event);
      sb.append("\n");
    }
  }

  public boolean tryToFindDate(String s, DateFormat datePattern) {
    try {
      datePattern.parse(s);
      return true;
    } catch (Exception e) {
    }
    return false;
  }

  private LogData tryToParseStringBuilder(StringBuilder sb, DateFormat datePattern) {
    LOGGER.finest("Trying to parse: " + sb.toString());
    if (sb.length() < 27) {
      LOGGER.finest("Length<27, return null");
      return null;
    }
    String dateString = sb.substring(0, 23);
    Date date = null;
    try {
      LOGGER.finest("Parsing date: \"" + dateString + "\"");
      date = datePattern.parse(dateString);
    } catch (Exception e) {
      LOGGER.warning("Date in \"" + dateString + "\" not parsed, return null, " + e.getMessage());
      return null;
    }
    if (date == null) {
      LOGGER.severe("Date parsed is null!");
      System.err.println("Date \"" + dateString + "\" not parsed!");
      return null;
    }
    LogData logData = new LogData();
    logData.setDate(date);

    int threadStartIndex = sb.indexOf("[");
    int threadEndIndex = sb.indexOf("]");

    String level = sb.substring(24, 30).trim();
    String thread = sb.substring(threadStartIndex + 1, threadEndIndex);
    logData.setClazz("");
    logData.setMessage("");
    logData.setThread(thread);
    logData.setLoggerName("");
    logData.setMessage(sb.substring(threadEndIndex + 1).trim());
    Level l = parseLevel(level);
    logData.setLevel(l);
    sb.setLength(0);
    return logData;

  }

  protected Level parseLevel(String s) {
    if (s.equalsIgnoreCase("INFO")) {
      return Level.INFO;
    } else if (s.equalsIgnoreCase("ERROR") || s.equalsIgnoreCase("FATAL")) {
      return Level.SEVERE;
    } else if (s.equalsIgnoreCase("WARN")) {
      return Level.WARNING;
    } else if (s.equalsIgnoreCase("DEBUG") || s.equalsIgnoreCase("TRACE")) {
      return Level.FINE;
    }
    LOGGER.severe("Level \"" + s + "\" not parsed!");
    return null;

  }

  @Override
  public ParserDescription getParserDescription() {
    return pd;
  }

  @Override
  public LogData parseBuffer(ParsingContext parsingContext) throws ParseException {
    return tryToParseStringBuilder(parsingContext.getUnmatchedLog(), parsingContext.getDateFormat());
  }

  @Override
  public void init(Properties properties) throws InitializationException {

  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
    parsingContext.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.ENGLISH));
  }

  @Override
  public int getVersion() {
    return LOG_PARSER_VERSION_1;
  }
}
