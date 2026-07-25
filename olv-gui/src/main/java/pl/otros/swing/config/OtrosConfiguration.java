package pl.otros.swing.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DataConfiguration;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventSource;
import org.apache.commons.configuration2.event.EventType;

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
  public <T extends Event> void addEventListener(EventType<T> eventType, EventListener<? super T> listener) {
    eventSource.addEventListener(eventType, listener);
  }

  @Override
  public <T extends Event> boolean removeEventListener(EventType<T> eventType, EventListener<? super T> listener) {
    return eventSource.removeEventListener(eventType, listener);
  }
}
