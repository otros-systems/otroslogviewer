package pl.otros.logview.api.pluginable;

import pl.otros.logview.api.services.StatsService;

public class LogFilterValueChangeListenerStatsWrapper implements LogFilterValueChangeListener {
  private final LogFilterValueChangeListener listener;
  private final StatsService statsService;
  private final String filterId;

  public LogFilterValueChangeListenerStatsWrapper(LogFilterValueChangeListener listener, StatsService statsService, String filterId) {
    this.listener = listener;
    this.statsService = statsService;
    this.filterId = filterId;
  }

  @Override
  public void valueChanged() {
    statsService.filterUsed(filterId);
    listener.valueChanged();
  }
}
