package pl.otros.logview.importer.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.LogDataCollector;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.importer.LogImporter;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogbackSocketLogImporter extends AbstractPluginableElement implements LogImporter {
  private static final Logger LOGGER = Logger.getLogger(LogbackSocketLogImporter.class.getName());

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
      LOGGER.log(Level.SEVERE, "Can't read serialized logback events from stream: " + e.getMessage(), e);
    }
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
