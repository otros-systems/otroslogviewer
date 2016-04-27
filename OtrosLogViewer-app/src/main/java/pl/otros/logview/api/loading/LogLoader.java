package pl.otros.logview.api.loading;


import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.model.LogDataCollector;

import java.util.Optional;

public interface LogLoader {

  LogLoadingSession startLoading(Source source, LogImporter logImporter, LogDataCollector logDataCollector);

  LogLoadingSession startLoading(Source source, LogImporter logImporter, LogDataCollector logDataCollector, long sleepTime, Optional<Long> bufferingTime);

  void pause(LogLoadingSession logLoadingSession);
  void resume(LogLoadingSession logLoadingSession);
  void stop(LogLoadingSession logLoadingSession);
  void close(LogLoadingSession logDataCollector);
  void close(LogDataCollector logDataCollector);
  void changeFilters(LogLoadingSession logLoadingSession,AcceptCondition acceptCondition);
  void changeFilters(LogDataCollector logDataCollector,AcceptCondition acceptCondition);
  LoadStatistic getLoadStatistic(LogLoadingSession logLoadingSession);
  LoadingDetails getLoadingDetails(LogDataCollector logDataCollector);
  void shutdown();
}
