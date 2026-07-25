package pl.otros.swing.config.provider;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import pl.otros.swing.config.ConfigView;

public interface ConfigurationProvider {
  Configuration getConfigurationForView(ConfigView configView) throws ConfigurationException;

  void reload(ConfigView configView) throws ConfigurationException;

  void save(ConfigView configView, Configuration configuration) throws ConfigurationException;
}
