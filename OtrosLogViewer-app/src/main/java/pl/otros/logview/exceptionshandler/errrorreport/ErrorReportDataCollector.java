package pl.otros.logview.exceptionshandler.errrorreport;

import java.util.Map;

public interface ErrorReportDataCollector {

  /**
   * @return Map of key/values for error report
   */
  Map<String, String> collect(ErrorReportCollectingContext context);

}
