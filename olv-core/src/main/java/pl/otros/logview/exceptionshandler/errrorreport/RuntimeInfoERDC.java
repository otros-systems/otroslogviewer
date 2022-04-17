package pl.otros.logview.exceptionshandler.errrorreport;

import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class RuntimeInfoERDC implements ErrorReportDataCollector {

  private static final String MEMORY_MAX = "RUNTIME:memory.max";
  private static final String MEMORY_FREE = "RUNTIME:memory.free";
  private static final String MEMORY_TOTAL = "RUNTIME:memory.total";
  private static final String PROCESSORS = "RUNTIME:processors.count";
  private static final String UP_TIME = "RUNTIME:uptime";
  private static final long MEGA_BYTE = 1024 * 1024;

  @Override
  public Map<String, String> collect(ErrorReportCollectingContext context) {
    NumberFormat nf = NumberFormat.getIntegerInstance();
    nf.setGroupingUsed(true);
    Runtime runtime = Runtime.getRuntime();
    HashMap<String, String> r = new HashMap<>();
    r.put(MEMORY_MAX, nf.format(runtime.maxMemory() / MEGA_BYTE) + "MB");
    r.put(MEMORY_FREE, nf.format(runtime.freeMemory() / MEGA_BYTE) + "MB");
    r.put(MEMORY_TOTAL, nf.format(runtime.totalMemory() / MEGA_BYTE) + "MB");
    r.put(PROCESSORS, nf.format(runtime.availableProcessors()));

    long upTime = ManagementFactory.getRuntimeMXBean().getUptime();
    r.put(UP_TIME, formatTimeDuration(upTime));
    return r;

  }

  protected String formatTimeDuration(final long timeInMs) {
    long timeInS = timeInMs / 1000;
    String upTimeFormatted = String.format("%d:%02d:%02d", timeInS / 3600, (timeInS % 3600) / 60, (timeInS % 60));
    return upTimeFormatted;
  }

}
