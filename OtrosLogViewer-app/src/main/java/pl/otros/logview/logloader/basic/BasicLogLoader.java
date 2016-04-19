package pl.otros.logview.logloader.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.loading.LoadStatistic;
import pl.otros.logview.api.loading.LogLoader;
import pl.otros.logview.api.loading.LogLoadingSession;
import pl.otros.logview.api.loading.Source;
import pl.otros.logview.api.model.LogDataCollector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BasicLogLoader implements LogLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicLogLoader.class);


  private final Map<String, LoadingRunnable> lrMap = new HashMap<>();
  private final Map<LogDataCollector, FilteringLogDataCollector> ldCollectorsMap = new HashMap<>();

  @Override
  public LogLoadingSession startLoading(Source source, LogImporter logImporter, LogDataCollector logDataCollector) {
    ldCollectorsMap.putIfAbsent(logDataCollector,new FilteringLogDataCollector(logDataCollector,Optional.empty()));
    final FilteringLogDataCollector filteringLogDataCollector = ldCollectorsMap.get(logDataCollector);

    final LoadingRunnable loadingRunnable = new LoadingRunnable(source, logImporter, filteringLogDataCollector, 100, Optional.empty());
    final Thread thread = new Thread(loadingRunnable);
    thread.setDaemon(true);
    thread.start();
    String id = UUID.randomUUID().toString(); //TODO replace this with something meaningful
    LogLoadingSession session = new LogLoadingSession(id, source);
    lrMap.put(id, loadingRunnable);
    LOGGER.info("Started {} ",id);
    return session;
  }

  @Override
  public void pause(LogLoadingSession logLoadingSession) {
    final String id = logLoadingSession.getId();
    if (lrMap.containsKey(id)) {
      LOGGER.info("Pausing {} ",id);
      final LoadingRunnable loadingRunnable = lrMap.get(id);
      loadingRunnable.pause();
    }
  }

  @Override
  public void resume(LogLoadingSession logLoadingSession) {
    final String id = logLoadingSession.getId();
    if (lrMap.containsKey(id)) {
      LOGGER.info("Resuming {} ",id);
      final LoadingRunnable loadingRunnable = lrMap.get(id);
      loadingRunnable.resume();
    }
  }

  @Override
  public void stop(LogLoadingSession logLoadingSession) {
    final String id = logLoadingSession.getId();
    if (lrMap.containsKey(id)) {
      LOGGER.info("Stopping {} ",id);
      final LoadingRunnable loadingRunnable = lrMap.get(id);
      loadingRunnable.stop();
    }
  }

  @Override
  public void close(LogLoadingSession logLoadingSession) {
    lrMap.computeIfPresent(logLoadingSession.getId(), (id, loadingRunnable) -> {
      LOGGER.info("Closing {} ",id);
      loadingRunnable.stop();
      return loadingRunnable;
    });
  }

  @Override
  public void close(LogDataCollector logDataCollector) {
    LOGGER.info("Closing {} TOOD should stop runnable!",logDataCollector);
    //TODO stop loading runnable
    ldCollectorsMap.remove(logDataCollector);
    logDataCollector.clear();
  }

  @Override
  public void changeFilters(LogLoadingSession logLoadingSession, AcceptCondition acceptCondition) {
    final String id = logLoadingSession.getId();
    if (lrMap.containsKey(id)) {
      LOGGER.info("Changing filter for {} to {}", id,acceptCondition);
      final LoadingRunnable loadingRunnable = lrMap.get(id);
      loadingRunnable.setFilter(Optional.of(acceptCondition));
    }

  }

  @Override
  public void changeFilters(LogDataCollector logDataCollector, AcceptCondition acceptCondition) {
    ldCollectorsMap.computeIfPresent(logDataCollector, (logDataCollector1, filteringLogDataCollector) -> {
      LOGGER.info("Changing filter for {} to {}",logDataCollector1,acceptCondition);
      filteringLogDataCollector.setAcceptCondition(Optional.of(acceptCondition));
      return filteringLogDataCollector;
    });
  }

  @Override
  public LoadStatistic getLoadStatistic(LogLoadingSession logLoadingSession) {

    final LoadingRunnable loadingRunnable = lrMap.get(logLoadingSession.getId());
    return loadingRunnable.getLoadStatistic();
  }

  @Override
  public void shutdown() {
    System.out.println("Shutting down");
    lrMap.values().stream().forEach(LoadingRunnable::stop);
    lrMap.clear();
    ldCollectorsMap.keySet().stream().forEach(LogDataCollector::clear);
    ldCollectorsMap.clear();
  }

}
