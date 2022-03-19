package pl.otros.swing.config;

import org.apache.commons.configuration.Configuration;

import javax.swing.*;

/**
 * This interface provides screen for configuration and it's name/description
 */
public interface ConfigView {
  /**
   * View ID, it will be used to create configuration file
   * @return view id
   */
  String getViewId();

  /**
   * Name of the screen
    * @return name of screen
   */
  String getName();

  /**
   * Description
   * @return description to be displayed below screen name
   */
  String getDescription();

  /**
   * Return JComponent representing view.
   * @return view component to display
   */
  JComponent getView();

  /**
   * Check if values are valid
   * @return run validation
   */
  ValidationResult validate();

  /**
   * Apply configuration. Notify view to run actions.
   */
  default void apply(){};

  /**
   * Loads UI state from configuration
   * @param c configuration
   */
  void loadConfiguration(Configuration c);

  /**
   * Saves UI state to configuration
   * @param c configuration
   */
  void saveConfiguration(final Configuration c);
}
