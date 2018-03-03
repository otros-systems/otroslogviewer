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

package pl.otros.logview.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

import static pl.otros.logview.api.TableColumns.*;

public class DetectOnTheFlyLogImporter extends AbstractPluginableElement implements LogImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DetectOnTheFlyLogImporter.class.getName());

  protected static final String PROPERTY_BYTE_BUFFER = "DetectInTheFlyLogImporter.byteBuffer";
  protected static final String PROPERTY_LOG_IMPORTER = "DetectInTheFlyLogImporter.logImporter";
  private int detectTryMinimum = 128;
  protected int detectTryMaximum = 200 * 1024;
  private final Collection<LogImporter> logImporters;

  public DetectOnTheFlyLogImporter(Collection<LogImporter> logImporters) {
    super("Autodetect log format", "Detect log format on the fly. Choose one of defined log importers.");
    this.logImporters = logImporters;
  }

  @Override
  public int getApiVersion() {
    return LOG_IMPORTER_VERSION_1;
  }

  @Override
  public void init(Properties properties) {

  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
    HashMap<String, Object> customContextsProperties = parsingContext.getCustomConextProperties();
    customContextsProperties.put(PROPERTY_BYTE_BUFFER, new ByteArrayOutputStream(detectTryMinimum));
  }

  @Override
  public void importLogs(InputStream in, LogDataCollector dataCollector, ParsingContext parsingContext) {
    HashMap<String, Object> customContextProperties = parsingContext.getCustomConextProperties();
    if (customContextProperties.containsKey(PROPERTY_LOG_IMPORTER)) {
      // Log importer detected, use it;
      LogImporter logImporter = (LogImporter) customContextProperties.get(PROPERTY_LOG_IMPORTER);
      LOGGER.debug(String.format("Have log importer detected (%s), will use it", logImporter.getName()));
      logImporter.importLogs(in, dataCollector, parsingContext);
    } else {
      try {
        byte[] buff = new byte[16 * 1024];
        int read;
        while ((read = in.read(buff)) > 0) {
          try (ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) customContextProperties.get(PROPERTY_BYTE_BUFFER)) {

            int totalRead = byteArrayOutputStream.size();
            totalRead += read;
            if (totalRead < detectTryMinimum) {
              LOGGER.debug(String.format("To small amount of data to detect log importer [%db]", totalRead));
              byteArrayOutputStream.write(buff, 0, read);
            } else if (totalRead > detectTryMaximum) {
              // stop parsing, protect of loading unlimited data
              parsingContext.setParsingInProgress(false);
              LOGGER.warn("Reached maximum size of log importer detection buffer, Will not load more data");
            } else {
              // try to detect log
              byteArrayOutputStream.write(buff, 0, read);
              LOGGER.debug("Trying to detect log importer");
              Optional<LogImporter> maybeLogImporter = Utils.detectLogImporter(logImporters, byteArrayOutputStream.toByteArray());

              if (maybeLogImporter.isPresent()) {
                final LogImporter detectLogImporter = maybeLogImporter.get();
                LOGGER.debug(String.format("Log importer detected (%s),this log importer will be used", detectLogImporter.getName()));
                detectLogImporter.initParsingContext(parsingContext);
                customContextProperties.put(PROPERTY_LOG_IMPORTER, detectLogImporter);
                byte[] buf = byteArrayOutputStream.toByteArray();
                
                PushbackInputStream pushbackWrapper = new PushbackInputStream(in, buf.length);
                pushbackWrapper.unread(buf);
                detectLogImporter.importLogs(pushbackWrapper, dataCollector, parsingContext);
                return;
              }
            }
          }

        }
      } catch (IOException e) {
        LOGGER.warn("IOException reading log file " + parsingContext.getLogSource());
      }

    }
  }

  @Override
  public TableColumns[] getTableColumnsToUse() {
    return new TableColumns[] { ID, TIME, LEVEL, MESSAGE, CLASS, METHOD, THREAD, MARK, FILE, LINE, NDC, PROPERTIES, LOGGER_NAME,
        LOG_SOURCE };
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
