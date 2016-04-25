/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.otros.logview.api.importer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.*;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Properties;

public class LogImporterUsingParser implements LogImporter, TableColumnNameSelfDescribable {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogImporterUsingParser.class.getName());
  private LogParser parser = null;

  private final ParserDescription pd;

  public LogImporterUsingParser(LogParser parser) {
    super();
    this.parser = parser;
    pd = parser.getParserDescription();
  }

  @Override
  public void init(Properties properties) throws InitializationException {
    parser.init(properties);
  }

  @Override
  public void importLogs(InputStream in, final LogDataCollector dataCollector, ParsingContext parsingContext) {
    LOGGER.trace("Log import started ");
    String line = null;
    LogData logData = null;
    String charset = parser.getParserDescription().getCharset();

    BufferedReader logReader = null;
    if (charset == null) {
      logReader = new BufferedReader(new InputStreamReader(in));
    } else {
      try {
        logReader = new BufferedReader(new InputStreamReader(in, charset));
      } catch (UnsupportedEncodingException e1) {
        LOGGER.error(String.format("Required charset [%s] is not supported: %s", charset, e1.getMessage()));
        LOGGER.info(String.format("Using default charset: %s", Charset.defaultCharset().displayName()));
        logReader = new BufferedReader(new InputStreamReader(in));
      }

    }
    while (true) {
      try {
        line = logReader.readLine();
        if (line == null) {
          break;
        }

        if (parser instanceof MultiLineLogParser) {
          synchronized (parsingContext) {
            logData = parser.parse(line, parsingContext);
          }
        } else {
          logData = parser.parse(line, parsingContext);
        }

        if (logData != null) {
          logData.setId(parsingContext.getGeneratedIdAndIncrease());
          logData.setLogSource(parsingContext.getLogSource());
          dataCollector.add(logData);
          parsingContext.setLastParsed(System.currentTimeMillis());
        }

      } catch (IOException e) {
        e.printStackTrace();
        LOGGER.error(String.format("IOException during log import (file %s): %s", parsingContext.getLogSource(), e.getMessage()));
        break;
      } catch (ParseException e) {
        LOGGER.error(String.format("ParseException during log import (file %s): %s", parsingContext.getLogSource(), e.getMessage()));
        e.printStackTrace();
        break;
      }
    }

    try {
      if (parser instanceof MultiLineLogParser) {
        MultiLineLogParser multiLineLogParser = (MultiLineLogParser) parser;
        logData = multiLineLogParser.parseBuffer(parsingContext);
        if (logData != null) {
          logData.setId(parsingContext.getGeneratedIdAndIncrease());
          logData.setLogSource(parsingContext.getLogSource());
          synchronized (parsingContext) {
            dataCollector.add(logData);
          }
          parsingContext.setLastParsed(System.currentTimeMillis());
        }
      }
    } catch (Exception e) {
      LOGGER.info("Cannot parser rest of buffer, probably stopped importing");
    } finally {
      IOUtils.closeQuietly(logReader);
    }

    LOGGER.trace("Log import finished!");
  }

  @Override
  public String getName() {
    return pd.getDisplayName();
  }

  @Override
  public String getKeyStrokeAccelelator() {
    return pd.getKeyStrokeAccelelator();
  }

  @Override
  public int getMnemonic() {
    return pd.getMenmonic();
  }

  @Override
  public Icon getIcon() {
    return pd.getIcon();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LogImporterUsingParser) {
      return parser.equals(((LogImporterUsingParser) obj).getParser());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return parser.hashCode();
  }

  public LogParser getParser() {
    return parser;
  }

  @Override
  public TableColumns[] getTableColumnsToUse() {
    TableColumns[] columns = TableColumns.ALL_WITHOUT_LOG_SOURCE;
    if (parser instanceof TableColumnNameSelfDescribable) {
      TableColumnNameSelfDescribable descriable = (TableColumnNameSelfDescribable) parser;
      columns = descriable.getTableColumnsToUse();
    }
    return columns;
  }

  @Override
  public String getDescription() {
    return pd.getDescription();
  }

  @Override
  public String getPluginableId() {
    return String.format("%s [%s]", this.getClass().getName(), pd.getFile());
  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
    parser.initParsingContext(parsingContext);
  }

  @Override
  public int getApiVersion() {
    return LOG_IMPORTER_VERSION_1;
  }

  @Override
  public String toString() {
    String s = super.toString();
    if (parser != null && parser.getParserDescription() != null) {
      s = parser.getParserDescription().getDisplayName();
    }
    return s;
  }
}
