package pl.otros.logview.logloader.basic;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import pl.otros.logview.BufferingLogDataCollectorProxy;
import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.io.LoadingInfo;
import pl.otros.logview.api.io.ObservableInputStreamImpl;
import pl.otros.logview.api.io.Utils;
import pl.otros.logview.api.loading.LoadStatistic;
import pl.otros.logview.api.loading.SocketSource;
import pl.otros.logview.api.loading.Source;
import pl.otros.logview.api.loading.VfsSource;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.ParsingContext;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Optional;

public class LoadingRunnable implements Runnable {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoadingRunnable.class);
  private final Source source;
  private final FilteringLogDataCollector logDataCollector;
  private final Optional<Long> bufferingTime;
  private final long sleepTime;

  private volatile boolean pause = false;
  private volatile boolean stop = false;
  private LogImporter importer;
  private long currentRead = 0;
  private long lastFileSize = 0;

  private enum SleepAction {Sleep, Break, Import}

  public LoadingRunnable(Source source, LogImporter logImporter, LogDataCollector logDataCollector, long sleepTime, Optional<Long> bufferingTime) {
    this(source, logImporter, logDataCollector, sleepTime, bufferingTime, Optional.empty());
  }

  public LoadingRunnable(Source source, LogImporter logImporter, LogDataCollector logDataCollector, long sleepTime, Optional<Long> bufferingTime, Optional<AcceptCondition> withAcceptCondition) {
    this.source = source;
    this.logDataCollector = new FilteringLogDataCollector(logDataCollector, withAcceptCondition);
    this.bufferingTime = bufferingTime;
    this.sleepTime = sleepTime;
    this.importer = logImporter;
  }

  @Override
  public void run() {
    if (source instanceof VfsSource) {
      runWithVfs((VfsSource) source);
    } else if (source instanceof SocketSource) {
      runWithSocket((SocketSource) source);
    } else {
      LOGGER.error("Not support source type: " + source);
    }
  }

  private void runWithSocket(SocketSource source) {
    final ParsingContext parsingContext = new ParsingContext("Socket", "Socket " + source.getSocket().getRemoteSocketAddress());
    importer.initParsingContext(parsingContext);
    try {
      final InputStream inputStream = new BufferedInputStream(source.getSocket().getInputStream());
      final ObservableInputStreamImpl observableInputStream = new ObservableInputStreamImpl(inputStream);

      final BaseConfiguration configuration = new BaseConfiguration();
      configuration.setProperty(ConfKeys.TAILING_PANEL_PLAY, true);
      final LogDataCollector collector = bufferingTime.map(t -> (LogDataCollector) new BufferingLogDataCollectorProxy(logDataCollector, t, configuration)).orElseGet(() -> logDataCollector);
      LOGGER.debug("Starting main loop");
      while (parsingContext.isParsingInProgress()) {
        try {
          SleepAction action;
          synchronized (this) {
            if (stop) {
              action = SleepAction.Break;
            } else if (pause) {
              action = SleepAction.Sleep;
            } else {
              action = SleepAction.Import;
            }
          }
          if (SleepAction.Sleep == action) {
            Thread.sleep(sleepTime);
          } else if (SleepAction.Break == action) {
            break;
          } else {
            importer.importLogs(observableInputStream, collector, parsingContext);
          }

          Thread.sleep(sleepTime);

        } catch (Exception e) {
          LOGGER.warn("Exception in tailing loop: " + e.getMessage());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    LOGGER.info("Log importing finished");
  }

  private void runWithVfs(VfsSource vfs) {
    try {
      final LoadingInfo loadingInfo = Utils.openFileObject(vfs.getFileObject(), true);
      final BaseConfiguration configuration = new BaseConfiguration();
      configuration.setProperty(ConfKeys.TAILING_PANEL_PLAY, true);
      LogDataCollector collector = bufferingTime.map(t -> (LogDataCollector) new BufferingLogDataCollectorProxy(logDataCollector, t, configuration)).orElseGet(() -> logDataCollector);
      ParsingContext parsingContext = new ParsingContext(
        loadingInfo.getFileObject().getName().getFriendlyURI(),
        loadingInfo.getFileObject().getName().getBaseName());

      importer.initParsingContext(parsingContext);
      try {
        loadingInfo.setLastFileSize(loadingInfo.getFileObject().getContent().getSize());
      } catch (FileSystemException e1) {
        LOGGER.warn("Can't initialize start position for tailing. Can duplicate some values for small files");
      }

      while (parsingContext.isParsingInProgress()) {
        try {
          SleepAction action;
          synchronized (this) {
            if (stop) {
              action = SleepAction.Break;
            } else if (pause) {
              action = SleepAction.Sleep;
            } else {
              action = SleepAction.Import;
            }
            currentRead = loadingInfo.getObserableInputStreamImpl().getCurrentRead();
            lastFileSize = loadingInfo.getLastFileSize();
          }
          if (SleepAction.Sleep == action) {
            Thread.sleep(sleepTime);
          } else if (SleepAction.Break == action) {
            LOGGER.debug("Log import stopped");
            break;
          } else {
            importer.importLogs(loadingInfo.getContentInputStream(), collector, parsingContext);
            if (!loadingInfo.isTailing() || loadingInfo.isGziped()) {
              break;
            }
          }

          synchronized (this) {
            currentRead = loadingInfo.getObserableInputStreamImpl().getCurrentRead();
            lastFileSize = loadingInfo.getLastFileSize();
          }
          Thread.sleep(sleepTime);
          Utils.reloadFileObject(loadingInfo);

        } catch (Exception e) {
          LOGGER.warn("Exception in tailing loop: " + e.getMessage());
        }
      }
      LOGGER.info(String.format("Loading of files %s is finished", loadingInfo.getFriendlyUrl()));
      parsingContext.setParsingInProgress(false);
      LOGGER.info("File " + loadingInfo.getFriendlyUrl() + " loaded");
      Utils.closeQuietly(loadingInfo.getFileObject());
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.error("Error when reading log", e);
    }
  }


  public synchronized LoadStatistic getLoadStatistic() {
    return new LoadStatistic(source, currentRead, lastFileSize);
  }


  public synchronized void pause() {
    pause = true;
  }

  public synchronized void resume() {
    pause = false;
  }

  public synchronized void stop() {
    stop = true;
  }

  public synchronized void setFilter(Optional<AcceptCondition> acceptCondition) {
    logDataCollector.setAcceptCondition(acceptCondition);
  }
}
