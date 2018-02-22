package pl.otros.logview.stats;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.services.PersistService;
import pl.otros.logview.api.services.StatsService;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DropwizardStatsService implements StatsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardStatsService.class.getName());
  public static final String STATS_COUNTERS = "stats.counters";
  private final MetricRegistry metrics = new MetricRegistry();
  private final PersistingReporter reporter;

  public DropwizardStatsService(PersistService persistService) {
    try {
      final CountersMap countersMap = persistService.load(STATS_COUNTERS, new CountersMap(), CountersMap.deserializer());
      LOGGER.info("Counters loaded");
      countersMap
        .getMap()
        .forEach((key, value) -> metrics.counter(key).inc(value));
    } catch (Exception ignore) {
      LOGGER.error("Can't load metrics, will use empty one", ignore);
    }

    reporter = new PersistingReporter(metrics, persistService);
    reporter.start(30, TimeUnit.SECONDS);
  }


  @Override
  public void actionExecuted(OtrosAction action) {
    metrics.counter("action:" + action.getClass().getName() + ".executed").inc();
  }

  @Override
  public void importLogsFromScheme(String scheme) {
    metrics.counter("imported:scheme:" + scheme).inc();
  }

  @Override
  public void filesImportedIntoOneView(int count) {
    metrics.counter("imported:filesToView:" + count).inc();
  }

  @Override
  public void logParserUsed(LogImporter logImporter) {
    if (logImporter instanceof LogImporterUsingParser) {
      LogImporterUsingParser parser = (LogImporterUsingParser) logImporter;
      metrics.counter("logParser:" + parser.getParser().getClass().getName() + ".used").inc();
    } else {
      metrics.counter("logParser:" + logImporter.getClass().getName() + ".used").inc();
    }
  }

  @Override
  public void bytesRead(String scheme, long bytes) {
    metrics.counter("io:parsedBytes." + scheme).inc(bytes);
  }

  @Override
  public void logEventsImported(String scheme, long count) {
    metrics.counter("io:importedLogEvents." + scheme).inc(count);
  }

  public Map<String, Long> getStats(){
    return reporter.countersMap(metrics.getCounters());

  }

}
