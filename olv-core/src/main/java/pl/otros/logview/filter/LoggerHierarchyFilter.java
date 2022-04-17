package pl.otros.logview.filter;

import pl.otros.logview.api.model.LogData;

import java.util.function.Function;

public class LoggerHierarchyFilter extends ClassLikeFilter {

  private Function<LogData,String> function = logData -> logData.getLoggerName();

  public LoggerHierarchyFilter() {
    super("Logger Filter", "Filtering events based on Logger hierarchy. It supports \"ignore\" and \"focus on\" mode.");
  }

  @Override
  public Function<LogData, String> extractValueFunction() {
    return function;
  }
}
