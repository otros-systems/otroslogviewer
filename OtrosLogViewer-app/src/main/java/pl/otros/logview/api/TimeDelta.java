package pl.otros.logview.api;

import java.util.Date;

public class TimeDelta {
  private final Date timestamp;

  public TimeDelta(Date timestamp) {
    this.timestamp = timestamp;
  }

  public Date getTimestamp() {
    return timestamp;
  }
}
