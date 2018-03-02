package pl.otros.logview.stats;

import com.codahale.metrics.*;
import pl.otros.logview.api.services.PersistService;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class PersistingReporter extends ScheduledReporter {

  private final PersistService persistService;

  PersistingReporter(MetricRegistry metrics, PersistService persistService) {
    super(metrics, "Persist", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    this.persistService = persistService;
  }

  @Override
  public void report(
    SortedMap<String, Gauge> gauges,
    SortedMap<String, Counter> counters,
    SortedMap<String, Histogram> histograms,
    SortedMap<String, Meter> meters,
    SortedMap<String, Timer> timers) {
    final Map<String, Long> collect = countersMap(counters);
    try {
      persistService.persist(DropwizardStatsService.STATS_COUNTERS, new CountersMap(collect), CountersMap.serializer());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public Map<String, Long> countersMap(SortedMap<String, Counter> counters) {
    return counters
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, c -> (c.getValue().getCount())));
  }
}
