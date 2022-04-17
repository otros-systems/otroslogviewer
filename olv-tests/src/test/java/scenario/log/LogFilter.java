package scenario.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LogFilter extends Filter<ILoggingEvent> {
  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (event.getLoggerName().startsWith("scenario") || event.getLevel().toInt() > Level.WARN_INT) {
      return FilterReply.ACCEPT;
    } else {
      return FilterReply.DENY;
    }
  }
}
