package pl.otros.logview.parser.json.log4j2;

import java.util.Map;

public class Log4j2JsonEvent {
  private long timeMillis;
  private String thread;
  private String level;
  private String loggerName;
  private String message;
  private Thrown thrown;
  private Source source;
  private Instant instant;

  public Map<String, String> getContextMap() {
    return contextMap;
  }

  private Map<String, String> contextMap;

  public long getTimeMillis() {
    return timeMillis;
  }

  public String getMessage() {
    return message;
  }

  public String getThread() {
    return thread;
  }

  public String getLevel() {
    return level;
  }

  public String getLoggerName() {
    return loggerName;
  }

  public Thrown getThrown() {
    return thrown;
  }

  public Source getSource() {
    return source;
  }

  public Instant getInstant() {
    return instant;
  }

  public static class Instant {
    private long epochSecond;
    private long nanoOfSecond;

    public Long timestamp() {
      return epochSecond * 1000 + nanoOfSecond / 1000000;
    }
  }
}
