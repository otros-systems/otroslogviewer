package pl.otros.logview.gui.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;

public class GeneralConfigView extends AbstractConfigView implements InMainConfig {


  private final JPanel panel;
  private final JCheckBox confirmQuit;

  public GeneralConfigView() {
    super("General", "General", "General configuration");
    panel = new JPanel(new MigLayout("left"));
    confirmQuit = new JCheckBox();
    addLabel("Confirm before exit", 'c', confirmQuit, panel);
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
  public void loadConfiguration(Configuration c) {
    confirmQuit.setSelected(c.getBoolean(ConfKeys.CONFIRM_QUIT, true));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(ConfKeys.CONFIRM_QUIT, confirmQuit.isSelected());
  }
}
