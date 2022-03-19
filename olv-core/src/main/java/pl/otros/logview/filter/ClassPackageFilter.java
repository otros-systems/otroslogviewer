package pl.otros.logview.filter;

import pl.otros.logview.api.model.LogData;

import java.util.function.Function;

public class ClassPackageFilter extends ClassLikeFilter {

  private Function<LogData,String> function = logData -> logData.getClazz();

  public ClassPackageFilter() {
    super("Class Filter", "Filtering events based on class/package. It supports \"ignore\" and \"focus on\" mode.");
  }

  @Override
  public Function<LogData,String> extractValueFunction() {
    return function;
  }

}
