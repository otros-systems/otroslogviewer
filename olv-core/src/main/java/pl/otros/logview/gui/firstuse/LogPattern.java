package pl.otros.logview.gui.firstuse;

import java.util.Properties;

public class LogPattern {
  private String pattern;
  private Properties properties;
  private boolean valid;

  LogPattern(String pattern, Properties properties, boolean valid) {
    this.pattern = pattern;
    this.properties = properties;
    this.valid = valid;
  }

  public String getPattern() {
    return pattern;
  }

  public boolean isValid() {
    return valid;
  }

  public Properties getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    return "LogPattern{" +
      "pattern='" + pattern + '\'' +
      ", properties=" + properties +
      ", valid=" + valid +
      '}';
  }
}
