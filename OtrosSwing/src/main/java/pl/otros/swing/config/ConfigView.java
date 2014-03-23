package pl.otros.swing.config;

import org.apache.commons.configuration.Configuration;

import javax.swing.*;

public interface ConfigView {
  String getViewId();

  String getName();

  String getDescirption();

  JComponent getView();

  ValidationResult validate();

  void loadConfguration(Configuration configuration);

  void saveConfiguration(final Configuration c);
}
