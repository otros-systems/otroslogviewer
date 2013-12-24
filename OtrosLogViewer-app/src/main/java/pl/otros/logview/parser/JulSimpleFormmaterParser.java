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
package pl.otros.logview.parser;

import pl.otros.logview.LogData;
import pl.otros.logview.gui.table.TableColumns;
import pl.otros.logview.importer.InitializationException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JulSimpleFormmaterParser implements MultiLineLogParser, TableColumnNameSelfDescribable {

  private static final String DATE_PATTERNS = "DATE_PATTERNS";

  private static final Logger LOGGER = Logger.getLogger(JulSimpleFormmaterParser.class.getName());

  private static final String ICON_PATH = "img/java.png";

  private ParserDescription pd;

  private LevelParser[] levelParser = new LevelParser[] { new LevelParser(Locale.ENGLISH), new LevelParser(Locale.GERMAN), new LevelParser(new Locale("es")) };

  public JulSimpleFormmaterParser() {
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
      LOGGER.warning("Error loading icon: " + e.getMessage());
    }
  }

  @Override
  public ParserDescription getParserDescription() {
    return pd;
  }

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
      LOGGER.warning("Exception: " + e.getClass());
      LOGGER.warning("Exception " + e.getMessage());
      LOGGER.warning("event: " + event);
      LOGGER.warning("Buffer: " + sb.toString());
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
      } catch (ParseException e) {
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
      } catch (ParseException e) {
      }
    }
    if (date == null) {
      LOGGER.warning("Date \"" + dateString + "\" not parsed!");
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
    for (LevelParser lp : this.levelParser) {
      Level l = lp.parse(level);
      if (l != null) {
        logData.setLevel(l);
      }
    }
    if (logData.getLevel() == null) {
      LOGGER.warning("Level not parsed!");
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
    SimpleDateFormat[] datePatterns = new SimpleDateFormat[] { new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH), new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN),
        new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", new Locale("en")), new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", new Locale("en")) };
    parsingContext.getCustomConextProperties().put(DATE_PATTERNS, datePatterns);

  }

  @Override
  public int getVersion() {
    return LOG_PARSER_VERSION_1;
  }

}
