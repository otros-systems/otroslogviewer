package pl.otros.logview.stats;

import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.services.StatsService;

public class StatsLogDataCollector implements LogDataCollector {

  private String scheme;
  private LogDataCollector dataCollector;
  private StatsService statsService;

  public StatsLogDataCollector(String scheme, LogDataCollector dataCollector, StatsService statsService) {
    this.scheme = scheme;
    this.dataCollector = dataCollector;
    this.statsService = statsService;
  }

  @Override
  public void add(LogData... logDatas) {
    statsService.logEventsImported(scheme, logDatas.length);
    dataCollector.add(logDatas);
  }

  @Override
  public LogData[] getLogData() {
    return dataCollector.getLogData();
  }

  @Override
  public int clear() {
    return dataCollector.clear();
  }
}
