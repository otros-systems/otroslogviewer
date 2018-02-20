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
package pl.otros.logview.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.parser.MultiLineLogParser;
import pl.otros.logview.api.parser.ParserDescription;
import pl.otros.logview.api.parser.ParsingContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

public class JulSimpleFormatterParser implements MultiLineLogParser {

  private static final String DATE_PATTERNS = "DATE_PATTERNS";

  private static final Logger LOGGER = LoggerFactory.getLogger(JulSimpleFormatterParser.class.getName());

  private static final String ICON_PATH = "img/java.png";

  private final ParserDescription pd;

  private final I18nLevelParser[] levelParser = {
    new I18nLevelParser(Locale.ENGLISH),
    new I18nLevelParser(Locale.GERMAN),
    new I18nLevelParser(new Locale("es")),
    new I18nLevelParser(new Locale("fr"))
  };

  public JulSimpleFormatterParser() {
    pd = new ParserDescription();
    pd.setDisplayName("JUL simple formatter");
    pd.setDescription("Parse logs formatted by java.util.logging with SimpleFormatter");
    pd.setKeyStrokeAccelelator("control j");
    pd.setMenmonic(KeyEvent.VK_J);
    pd.setFile(this.getClass().getName());
    try {
      Icon icon = new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(ICON_PATH)));
      pd.setIcon(icon);
      LOGGER.info("icon loaded");
    } catch (Exception e) {
      LOGGER.warn("Error loading icon: " + e.getMessage());
    }
  }

  @Override
  public ParserDescription getParserDescription() {
    return pd;
  }

  @Override
  public LogData parse(String event, ParsingContext context) throws ParseException {
    if (event == null) {
      return null;
    }
    StringBuilder sb = context.getUnmatchedLog();
    SimpleDateFormat[] datePatterns = (SimpleDateFormat[]) context.getCustomConextProperties().get(DATE_PATTERNS);
    try {
      if (!tryToFindDate(event, datePatterns)) {
        return null;
      }
      return tryToParseStringBuilder(sb, datePatterns);
    } catch (IndexOutOfBoundsException e) {
      return null;
    } catch (NumberFormatException e) {
      // e.printStackTrace();
      return null;
    } catch (Exception e) {
      LOGGER.warn("Exception: " + e.getClass());
      LOGGER.warn("Exception " + e.getMessage());
      LOGGER.warn("event: " + event);
      LOGGER.warn("Buffer: " + sb.toString());
      return null;
    } finally {
      sb.append(event);
      sb.append("\n");
    }
  }

  public boolean tryToFindDate(String s, SimpleDateFormat[] datePatterns) {
    for (SimpleDateFormat pl : datePatterns) {
      try {
        pl.parse(s);
        return true;
      } catch (ParseException ignored) {
      }
    }
    return false;
  }

  private LogData tryToParseStringBuilder(StringBuilder sb, SimpleDateFormat[] datePatterns) {
    String dateString = sb.substring(0, (sb.length() >= 25 ? 25 : sb.length()));
    if (dateString.length() == 0) {
      return null;
    }
    // 2010-02-11 16:08:06 d.bf.g.A main
    Date date = null;
    for (SimpleDateFormat pl : datePatterns) {
      try {
        date = pl.parse(dateString);
        int dateLength = pl.format(date).length();
        sb.replace(0, dateLength + 1, "");
        break;
      } catch (ParseException ignored) {
      }
    }
    if (date == null) {
      LOGGER.warn("Date \"" + dateString + "\" not parsed!");
      return null;
    }
    LogData logData = new LogData();
    logData.setDate(date);
    int firsLineIndex = sb.indexOf("\n");
    String classMethodLevel = sb.substring(0, firsLineIndex);
    logData.setLoggerName(classMethodLevel.split(" ")[0]);
    String clazz = classMethodLevel.split(" ")[0];
    String method = classMethodLevel.split(" ")[1];
    String levelAndMessage = sb.substring(firsLineIndex + 1);
    String level = levelAndMessage.substring(0, levelAndMessage.indexOf(':'));
    logData.setMethod(method);
    logData.setClazz(clazz);
    logData.setMessage(levelAndMessage.substring(levelAndMessage.indexOf(':') + 1).trim());
    for (I18nLevelParser lp : this.levelParser) {
      Level l = lp.parse(level);
      if (l != null) {
        logData.setLevel(l);
      }
    }
    if (logData.getLevel() == null) {
      LOGGER.warn("Level not parsed!");
      return null;
    }
    sb.setLength(0);
    return logData;

  }

  @Override
  public LogData parseBuffer(ParsingContext parsingContext) throws ParseException {
    LogData ld = tryToParseStringBuilder(parsingContext.getUnmatchedLog(), (SimpleDateFormat[]) parsingContext.getCustomConextProperties().get(DATE_PATTERNS));
    if (ld != null) {
      parsingContext.getUnmatchedLog().setLength(0);
    }
    return ld;
  }

  @Override
  public void init(Properties properties) throws InitializationException {

  }

  @Override
  public TableColumns[] getTableColumnsToUse() {
    return TableColumns.JUL_COLUMNS;
  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
    SimpleDateFormat[] datePatterns = {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
      new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH), new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN),
      new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", new Locale("en")), new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", new Locale("en")),
      new SimpleDateFormat("dd MMM yyyy HH:mm:ss", new Locale("fr"))
    };
    parsingContext.getCustomConextProperties().put(DATE_PATTERNS, datePatterns);

  }

  @Override
  public int getVersion() {
    return LOG_PARSER_VERSION_1;
  }

}
