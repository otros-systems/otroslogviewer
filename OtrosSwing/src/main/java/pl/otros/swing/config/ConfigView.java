package pl.otros.swing.config;

import org.apache.commons.configuration.Configuration;

import javax.swing.*;

/**
 * This interface provides screen for configuration and it's name/description
 */
public interface ConfigView {
  /**
   * View ID, it will be used to create configuration file
   * @return
   */
  String getViewId();

  /**
   * Name of the screen
    * @return
   */
  String getName();

  /**
   * Description
   * @return
   */
  String getDescription();

  /**
   * Return JComponent representing view.
   * @return
   */
  JComponent getView();

  /**
   * Check if values are valid
   * @return
   */
  ValidationResult validate();

  /**
   * Loads UI state from configuration
   * @param c
   */
  void loadConfiguration(Configuration c);

  /**
   * Saves UI state to configuration
   * @param c
   */
  void saveConfiguration(final Configuration c);
}
