package pl.otros.logview;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.event.EventSource;

import java.util.Collection;

public class OtrosConfiguration extends DataConfiguration {
  private final EventSource eventSource;

  /**
   * Creates a new instance of {@code DataConfiguration} and sets the
   * wrapped configuration.
   *
   * @param configuration the wrapped configuration
   */
  public OtrosConfiguration(Configuration configuration) {
    super(configuration);
    if (configuration instanceof EventSource) {
      eventSource = (EventSource) configuration;
    } else {
      eventSource = this;
    }

  }

  @Override
  public void addConfigurationListener(ConfigurationListener l) {
    eventSource.addConfigurationListener(l);
  }

  @Override
  public boolean removeConfigurationListener(ConfigurationListener l) {
    return eventSource.removeConfigurationListener(l);
  }

  @Override
  public void addErrorListener(ConfigurationErrorListener l) {
    eventSource.addErrorListener(l);
  }

  @Override
  public void clearErrorListeners() {
    eventSource.clearErrorListeners();
  }

  @Override
  public boolean removeErrorListener(ConfigurationErrorListener l) {
    return eventSource.removeErrorListener(l);
  }

  @Override
  public Collection<ConfigurationErrorListener> getErrorListeners() {
    return eventSource.getErrorListeners();
  }

  @Override
  public void clearConfigurationListeners() {
    eventSource.clearConfigurationListeners();
  }

  @Override
  public Collection<ConfigurationListener> getConfigurationListeners() {
    return eventSource.getConfigurationListeners();
  }
}
