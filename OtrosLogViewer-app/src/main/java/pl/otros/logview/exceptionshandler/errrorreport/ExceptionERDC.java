package pl.otros.logview.exceptionshandler.errrorreport;

import com.google.common.base.Throwables;

import java.util.HashMap;
import java.util.Map;


public class ExceptionERDC implements ErrorReportDataCollector {

  public static final String EXCEPTION = "EXCEPTION:exception";
  public static final String MESSAGE = "EXCEPTION:message";
  public static final String THREAD = "EXCEPTION:thread";
  public static final String MESSAGE_LOCALIZED = "EXCEPTION:message.localized";
  public static final String STACKTRACE = "EXCEPTION:stacktrace";

  @Override
    public Map<String, String> collect(ErrorReportCollectingContext context) {
        HashMap<String, String> r = new HashMap<>();
        Throwable throwable = context.getThrowable();
        r.put(EXCEPTION, throwable.getClass().getName());
        r.put(MESSAGE, throwable.getMessage());
        r.put(THREAD, context.getThread().getName());
        r.put(MESSAGE_LOCALIZED, throwable.getLocalizedMessage());
        r.put(STACKTRACE, Throwables.getStackTraceAsString(throwable));
        return r;
    }

}
