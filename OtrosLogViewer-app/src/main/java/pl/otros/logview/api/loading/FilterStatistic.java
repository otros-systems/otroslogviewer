package pl.otros.logview.api.loading;

import java.util.Date;

public class FilterStatistic {

  private LogLoadingSession logLoadingSession;
  private Date date;
  private long processed = 0;
  private long passed = 0;
  private long rejected = 0;

  public FilterStatistic(LogLoadingSession logLoadingSession, long processed, long passed, long rejected) {
    this.logLoadingSession = logLoadingSession;
    this.processed = processed;
    this.passed = passed;
    this.rejected = rejected;
    this.date = new Date();
  }

  public LogLoadingSession getLogLoadingSession() {
    return logLoadingSession;
  }

  public Date getDate() {
    return date;
  }

  public long getProcessed() {
    return processed;
  }

  public long getPassed() {
    return passed;
  }

  public long getRejected() {
    return rejected;
  }
}
