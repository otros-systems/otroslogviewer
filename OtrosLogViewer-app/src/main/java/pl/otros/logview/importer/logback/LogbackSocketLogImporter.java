package pl.otros.logview.importer.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.model.LogDataBuilder;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.swing.Icon;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Properties;

import static pl.otros.logview.api.TableColumns.CLASS;
import static pl.otros.logview.api.TableColumns.ID;
import static pl.otros.logview.api.TableColumns.LEVEL;
import static pl.otros.logview.api.TableColumns.LINE;
import static pl.otros.logview.api.TableColumns.LOGGER_NAME;
import static pl.otros.logview.api.TableColumns.MARK;
import static pl.otros.logview.api.TableColumns.MESSAGE;
import static pl.otros.logview.api.TableColumns.METHOD;
import static pl.otros.logview.api.TableColumns.PROPERTIES;
import static pl.otros.logview.api.TableColumns.THREAD;
import static pl.otros.logview.api.TableColumns.TIME;

public class LogbackSocketLogImporter extends AbstractPluginableElement implements LogImporter {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogbackSocketLogImporter.class.getName());

  public LogbackSocketLogImporter() {
    super("Logback - socket", "Logback serialized events for socket appender");
  }

  @Override
  public int getApiVersion() {
    return LOG_IMPORTER_VERSION_1;
  }

  @Override
  public void init(Properties properties) throws InitializationException {
    LOGGER.info("LogbackSocketLogImporter.init\n");
  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
  }

  @Override
  public void importLogs(InputStream in, LogDataCollector dataCollector, ParsingContext parsingContext) {
    try {
      ObjectInputStream oin = new ObjectInputStream(in);
      while (parsingContext.isParsingInProgress()) {
        final Object o = oin.readObject();
        LOGGER.info("LogbackSocketLogImporter.importLogs have read object " + o + "\n");
        if (o instanceof ILoggingEvent) {
          ILoggingEvent ev = (ILoggingEvent) o;
          final LogDataBuilder builder = LogbackUtil.translate(ev)
            .withId(parsingContext.getGeneratedIdAndIncrease());
          dataCollector.add(builder.build());
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      LOGGER.error("Can't read serialized logback events from stream: " + e.getMessage(), e);
    }
  }

  @Override
  public TableColumns[] getTableColumnsToUse() {
    return new TableColumns[] { ID, LOGGER_NAME, MESSAGE, LEVEL, PROPERTIES, TIME, THREAD, MARK, CLASS, METHOD, LINE };
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
