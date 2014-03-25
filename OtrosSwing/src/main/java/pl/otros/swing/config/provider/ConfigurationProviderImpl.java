package pl.otros.swing.config.provider;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import pl.otros.swing.config.ConfigView;
import pl.otros.swing.config.InMainConfig;

import java.io.File;

public class ConfigurationProviderImpl implements ConfigurationProvider {
  private Configuration mainConfiguration;
  private File parent;

  public ConfigurationProviderImpl(Configuration mainConfiguration, File dir) {
    super();
    this.mainConfiguration = mainConfiguration;
    this.parent = dir;
    if (!parent.exists()) {
      parent.mkdirs();
    }
  }

  @Override
  public Configuration getConfigurationForView(ConfigView configView) throws ConfigurationException {
    Configuration c;
    if (configView instanceof InMainConfig) {
      c = mainConfiguration;
    } else {
      c = new PropertiesConfiguration(new File(parent, "config-"+configView.getViewId() + ".properties"));
    }
    return c;
  }
}
