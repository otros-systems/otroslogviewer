package pl.otros.logview.gui.firstuse;

import java.util.Collection;

public class LogPatterns {
  private final Collection<LogPattern> logPatterns;

  LogPatterns(Collection<LogPattern> logPatterns) {
    this.logPatterns = logPatterns;
  }

  public Collection<LogPattern> getLogPatterns() {
    return logPatterns;
  }
}
