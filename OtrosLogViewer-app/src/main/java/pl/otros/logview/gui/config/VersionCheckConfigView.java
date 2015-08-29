package pl.otros.logview.gui.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;

public class VersionCheckConfigView extends AbstractConfigView implements InMainConfig {
  private final JCheckBox checkCombo;
  private final JPanel panel;

  public VersionCheckConfigView() {
    super("versionCheck", "Version check", "Check for new version");
    panel = new JPanel(new MigLayout("left"));
    checkCombo = new JCheckBox();
    addLabel("Check for new version on startup", 'c', checkCombo, panel);
  }

  @Override
  public JComponent getView() {
    return panel;
  }

  @Override
  public ValidationResult validate() {
    return new ValidationResult();
  }

  @Override
  public void loadConfiguration(Configuration configuration) {
    checkCombo.setSelected(configuration.getBoolean(ConfKeys.VERSION_CHECK_ON_STARTUP, true));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(ConfKeys.VERSION_CHECK_ON_STARTUP, checkCombo.isSelected());
  }
}
