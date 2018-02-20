/*******************************************************************************
 * Copyright 2012 Krzysztof Otrebski
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

package pl.otros.logview.importer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.parser.log4j.Log4jUtil;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Properties;

import static pl.otros.logview.api.TableColumns.CLASS;
import static pl.otros.logview.api.TableColumns.FILE;
import static pl.otros.logview.api.TableColumns.ID;
import static pl.otros.logview.api.TableColumns.LEVEL;
import static pl.otros.logview.api.TableColumns.LINE;
import static pl.otros.logview.api.TableColumns.LOGGER_NAME;
import static pl.otros.logview.api.TableColumns.LOG_SOURCE;
import static pl.otros.logview.api.TableColumns.MESSAGE;
import static pl.otros.logview.api.TableColumns.METHOD;
import static pl.otros.logview.api.TableColumns.NDC;
import static pl.otros.logview.api.TableColumns.PROPERTIES;
import static pl.otros.logview.api.TableColumns.THREAD;
import static pl.otros.logview.api.TableColumns.TIME;

public class Log4jSerilizedLogImporter extends AbstractPluginableElement implements LogImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(Log4jSerilizedLogImporter.class.getName());

  private static final String DESCRIPTION = "Log4j serialized events";
  private static final String NAME = "Log4j serialized events used org.apache.log4j.net.SocketAppenders";

  public Log4jSerilizedLogImporter() {
    super(NAME, DESCRIPTION);
  }

  @Override
  public int getApiVersion() {
    return LOG_IMPORTER_VERSION_1;
  }

  @Override
  public void init(Properties properties) throws InitializationException {

  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {

  }

  @Override
  public void importLogs(InputStream in, LogDataCollector dataCollector, ParsingContext parsingContext) {
    try (ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in))) {
      LoggingEvent le;
      while ((le = (LoggingEvent) oin.readObject()) != null) {
        LogData translateLog4j = Log4jUtil.translateLog4j(le);
        translateLog4j.setLogSource(parsingContext.getLogSource());
        dataCollector.add(translateLog4j);
      }
    } catch (IOException e) {
      LOGGER.warn(String.format("IOException when reading log4j serialized event: %s", e.getMessage()));
    } catch (ClassNotFoundException e) {
      LOGGER.warn(String.format("ClassNotFoundException when reading log4j serialized event %s", e.getMessage()));
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  public TableColumns[] getTableColumnsToUse() {
    return new TableColumns[] { ID, TIME, MESSAGE, LEVEL, CLASS, METHOD, FILE, LINE, NDC, THREAD, LOGGER_NAME, PROPERTIES, LOG_SOURCE };
  }

  @Override
  public String getKeyStrokeAccelelator() {
    return null;
  }

  @Override
  public int getMnemonic() {
    return 0;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

}
