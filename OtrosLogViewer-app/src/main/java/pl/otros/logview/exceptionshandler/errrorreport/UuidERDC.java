package pl.otros.logview.exceptionshandler.errrorreport;

import pl.otros.logview.gui.ConfKeys;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects UUID or "?" if UUID is not present in configuration
 */
public class UuidERDC implements ErrorReportDataCollector {
    @Override
    public Map<String, String> collect(ErrorReportCollectingContext context) {
        Map<String, String> r = new HashMap<>();
        r.put("APPLICATION:uuid", context.getOtrosApplication().getConfiguration().getString(ConfKeys.UUID, "?"));
        return r;
    }
}
