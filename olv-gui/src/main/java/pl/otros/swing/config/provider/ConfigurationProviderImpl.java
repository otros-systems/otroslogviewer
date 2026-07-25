package pl.otros.swing.config.provider;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import pl.otros.swing.config.ConfigView;
import pl.otros.swing.config.InMainConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationProviderImpl implements ConfigurationProvider {
  private final Configuration mainConfiguration;
  private final File parent;
  private final Map<ConfigView, FileBasedConfigurationBuilder<PropertiesConfiguration>> builders = new HashMap<>();

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
    if (configView instanceof InMainConfig) {
      return mainConfiguration;
    } else {
      if (!builders.containsKey(configView)) {
        File file = new File(parent, "config-" + configView.getViewId() + ".properties");
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
          new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
            .configure(new Parameters().properties()
              .setFile(file));
        builders.put(configView, builder);
      }
      return builders.get(configView).getConfiguration();
    }
  }

  @Override
  public void reload(ConfigView configView) throws ConfigurationException {
    if (configView instanceof InMainConfig) {
      // Main configuration is usually managed outside, but if it's file based we might want to reload it.
      // However, OtrosApplication doesn't expose a builder for the main config easily here.
    } else {
      FileBasedConfigurationBuilder<PropertiesConfiguration> builder = builders.get(configView);
      if (builder != null) {
        builder.resetResult();
      }
    }
  }

  @Override
  public void save(ConfigView configView, Configuration configuration) throws ConfigurationException {
    if (configView instanceof InMainConfig) {
      if (mainConfiguration instanceof FileBasedConfiguration) {
        // This is still tricky without a builder.
      }
    } else {
      FileBasedConfigurationBuilder<PropertiesConfiguration> builder = builders.get(configView);
      if (builder != null) {
        builder.save();
      }
    }
  }
}
