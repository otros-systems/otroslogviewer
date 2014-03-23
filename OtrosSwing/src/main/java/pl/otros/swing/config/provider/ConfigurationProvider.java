package pl.otros.swing.config.provider;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import pl.otros.swing.config.ConfigView;

public interface ConfigurationProvider {
  public Configuration getConfigurationForView(ConfigView configView) throws ConfigurationException;
}
