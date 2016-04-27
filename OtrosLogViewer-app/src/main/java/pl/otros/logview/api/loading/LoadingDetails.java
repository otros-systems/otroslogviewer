package pl.otros.logview.api.loading;

import pl.otros.logview.api.model.LogDataCollector;

import java.util.List;

public class LoadingDetails {

  private List<LogLoadingSession> logLoadingSessions;
  private LogDataCollector logDataCollector;

  public LoadingDetails(LogDataCollector logDataCollector, List<LogLoadingSession> logLoadingSessions) {
    this.logDataCollector = logDataCollector;
    this.logLoadingSessions = logLoadingSessions;
  }

  public List<LogLoadingSession> getLogLoadingSessions() {
    return logLoadingSessions;
  }

  public LogDataCollector getLogDataCollector() {
    return logDataCollector;
  }
}
