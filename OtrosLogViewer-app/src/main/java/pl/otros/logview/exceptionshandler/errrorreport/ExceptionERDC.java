package pl.otros.logview.exceptionshandler.errrorreport;

import com.google.common.base.Throwables;

import java.util.HashMap;
import java.util.Map;


public class ExceptionERDC implements ErrorReportDataCollector {

    @Override
    public Map<String, String> collect(ErrorReportCollectingContext context) {
        HashMap<String, String> r = new HashMap<String, String>();
        Throwable throwable = context.getThrowable();
        r.put("EXCEPTION:exception", throwable.getClass().getName());
        r.put("EXCEPTION:message", throwable.getMessage());
        r.put("EXCEPTION:thread", context.getThread().getName());
        r.put("EXCEPTION:message.localized", throwable.getLocalizedMessage());
        r.put("EXCEPTION:stacktrace", Throwables.getStackTraceAsString(throwable));
        return r;
    }

}
