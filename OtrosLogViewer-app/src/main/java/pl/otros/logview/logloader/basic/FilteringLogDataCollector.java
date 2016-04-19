package pl.otros.logview.logloader.basic;

import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FilteringLogDataCollector implements LogDataCollector {

  private final LogDataCollector dataCollector;

  public void setAcceptCondition(Optional<AcceptCondition> acceptCondition) {
    this.acceptCondition = acceptCondition;
  }

  private Optional<AcceptCondition> acceptCondition;

  public FilteringLogDataCollector(LogDataCollector dataCollector, Optional<AcceptCondition> acceptCondition) {
    this.dataCollector = dataCollector;
    this.acceptCondition = acceptCondition;
  }

  @Override
  public void add(LogData... logDatas) {
    if (acceptCondition.isPresent()){
      final AcceptCondition acceptCondition = this.acceptCondition.get();
      final List<LogData> collect = Arrays.asList(logDatas).stream().filter(acceptCondition::accept).collect(Collectors.toList());
      final LogData[] filtered = collect.toArray(new LogData[collect.size()]);
      dataCollector.add(filtered);
    } else {
      dataCollector.add(logDatas);
    }
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
