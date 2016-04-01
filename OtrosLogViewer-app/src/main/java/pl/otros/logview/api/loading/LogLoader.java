package pl.otros.logview.api.loading;


import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.LogParser;

public interface LogLoader {
  LogLoadingSession startLoading(Source source, LogParser logParser, LogDataCollector logDataCollector);
  void pause(LogLoadingSession logLoadingSession);
  void resume(LogLoadingSession logLoadingSession);
  void stop(LogLoadingSession logLoadingSession);
  void close(LogLoadingSession logDataCollector);
  void close(LogDataCollector logDataCollector);
  void changeFilters(LogLoadingSession logLoadingSession,AcceptCondition acceptCondition);
  void changeFilters(LogDataCollector logDataCollector,AcceptCondition acceptCondition);
  LoadStatistic getLoadStatistic(LogLoadingSession logLoadingSession);
  void shutdown();
}
