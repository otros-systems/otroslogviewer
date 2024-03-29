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
import pl.otros.logview.api.services.StatsService;
import pl.otros.logview.stats.StatsLogDataCollector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class LoadingRunnable implements Runnable {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoadingRunnable.class);
  private final Source source;
  private StatsService statsService;
  private final LogDataCollector logDataCollector;
  private final Optional<Long> bufferingTime;
  private final long sleepTime;

  private volatile boolean pause = false;
  private volatile boolean stop = false;
  private LogImporter importer;
  private long currentRead = 0;
  private long lastFileSize = 0;
  private Optional<ObservableInputStreamImpl> obserableInputStreamImpl = Optional.empty();
  private FilteringLogDataCollector filteringLogDataCollector;

  private enum SleepAction {Sleep, Break, Import}

  public LoadingRunnable(Source source, LogImporter logImporter, LogDataCollector logDataCollector, long sleepTime, Optional<Long> bufferingTime, StatsService statsService) {
    this(source, logImporter, logDataCollector, sleepTime, bufferingTime, Optional.empty(), statsService);
  }

  public LoadingRunnable(Source source, LogImporter logImporter, LogDataCollector logDataCollector, long sleepTime, Optional<Long> bufferingTime, Optional<AcceptCondition> withAcceptCondition, StatsService statsService) {
    this.source = source;
    this.statsService = statsService;
    filteringLogDataCollector = new FilteringLogDataCollector(logDataCollector, withAcceptCondition);
    this.bufferingTime = bufferingTime;
    this.sleepTime = sleepTime;
    this.importer = logImporter;
    String scheme = "";
    if (source instanceof VfsSource) {
      scheme = ((VfsSource)source).getFileObject().getName().getScheme();
    } else if (source instanceof SocketSource) {
      scheme = "socket";
    }
    this.logDataCollector = new StatsLogDataCollector(scheme, filteringLogDataCollector, statsService);
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
    statsService.importLogsFromScheme("socket");
    statsService.logParserUsed(importer);
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
            Long beforeRead = obserableInputStreamImpl.map(ObservableInputStreamImpl::getCurrentRead).orElse(0L);
            importer.importLogs(observableInputStream, collector, parsingContext);
            Long afterRead = obserableInputStreamImpl.map(ObservableInputStreamImpl::getCurrentRead).orElse(0L);
            statsService.bytesRead("socket", afterRead - beforeRead);
          }

        } catch (Exception e) {
          LOGGER.warn("Exception in tailing loop: " + e.getMessage());
        } finally {
          Thread.sleep(sleepTime);
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error occured when importing logs from socket", e);
    }
    LOGGER.info("Log importing finished");
  }

  private void runWithVfs(VfsSource vfs) {
    try {
      statsService.importLogsFromScheme(vfs.getFileObject().getName().getScheme());
      statsService.logParserUsed(importer);
      final LoadingInfo loadingInfo = new LoadingInfo(vfs.getFileObject(), true, vfs.getOpenMode());
      final BaseConfiguration configuration = new BaseConfiguration();
      configuration.setProperty(ConfKeys.TAILING_PANEL_PLAY, true);
      LogDataCollector collector = bufferingTime.map(t -> (LogDataCollector) new BufferingLogDataCollectorProxy(logDataCollector, t, configuration)).orElse(logDataCollector);
      ParsingContext parsingContext = new ParsingContext(
        loadingInfo.getFileObject().getName().getFriendlyURI(),
        loadingInfo.getFileObject().getName().getBaseName());

      importer.initParsingContext(parsingContext);
      try {
        loadingInfo.resetLastFileSize();
      } catch (IOException e1) {
        LOGGER.warn("Can't initialize start position for tailing. Can duplicate some values for small files");
      }

      while (parsingContext.isParsingInProgress()) {
        try {
          SleepAction action;
          obserableInputStreamImpl = Optional.of(loadingInfo.getObservableInputStreamImpl());

          synchronized (this) {
            if (stop) {
              action = SleepAction.Break;
            } else if (pause) {
              action = SleepAction.Sleep;
            } else {
              action = SleepAction.Import;
            }

            updateStats(loadingInfo);
          }
          if (SleepAction.Sleep == action) {
            Thread.sleep(sleepTime);
          } else if (SleepAction.Break == action) {
            LOGGER.debug("Log import stopped");
            break;
          } else {
            Long beforeRead = obserableInputStreamImpl.map(ObservableInputStreamImpl::getCurrentRead).orElse(0L);
            importer.importLogs(loadingInfo.getContentInputStream(), collector, parsingContext);
            Long afterRead = obserableInputStreamImpl.map(ObservableInputStreamImpl::getCurrentRead).orElse(0L);
            statsService.bytesRead(loadingInfo.getFileObject().getName().getScheme(), afterRead - beforeRead);
            if (!loadingInfo.isTailing() || loadingInfo.getCompression().isCompressed()) {
              break;
            }
          }

          Thread.sleep(sleepTime);
          loadingInfo.reloadIfFileSizeChanged();
          updateStats(loadingInfo);
          loadingInfo.reloadIfFileSizeChanged();

        } catch (Exception e) {
          LOGGER.warn("Exception in tailing loop: " + e.getMessage());
          Thread.sleep(sleepTime);
        }
      }
      LOGGER.info(String.format("Loading of files %s is finished", loadingInfo.getFriendlyUrl()));
      parsingContext.setParsingInProgress(false);
      LOGGER.info("File " + loadingInfo.getFriendlyUrl() + " loaded");
      loadingInfo.close();
    } catch (Exception e) {
      LOGGER.error("Error when reading log", e);
    } finally {
      Utils.closeQuietly(vfs.getFileObject());
    }
  }

  private synchronized void updateStats(LoadingInfo loadingInfo) {
    obserableInputStreamImpl.ifPresent(in -> currentRead = in.getCurrentRead());
    try {
      lastFileSize = loadingInfo.getFileObject().getContent().getSize();
    } catch (FileSystemException ex) {
      LOGGER.debug("Can't update stats");
    }
  }

  public synchronized LoadStatistic getLoadStatistic() {
    final Long position = obserableInputStreamImpl.map(ObservableInputStreamImpl::getCurrentRead).orElse(0L);
    return new LoadStatistic(source, position, lastFileSize);
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
    filteringLogDataCollector.setAcceptCondition(acceptCondition);
  }
}
