package pl.otros.swing.config;

import org.apache.commons.configuration.Configuration;

import javax.swing.*;

public interface ConfigView {
  String getViewId();

  String getName();

  String getDescription();

  JComponent getView();

  ValidationResult validate();

  void loadConfiguration(Configuration configuration);

  void saveConfiguration(final Configuration c);
}
