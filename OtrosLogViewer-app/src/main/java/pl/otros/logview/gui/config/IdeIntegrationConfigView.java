package pl.otros.logview.gui.config;

import org.apache.commons.configuration.Configuration;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.ide.IdeIntegrationConfigurationPanel;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;

public class IdeIntegrationConfigView extends AbstractConfigView implements InMainConfig{

  private final IdeIntegrationConfigurationPanel ideIntegrationConfigurationPanel;

  public IdeIntegrationConfigView(OtrosApplication otrosApplication) {
    super("ideIntegration", "IDE integration", "Integration with IDE (IntelliJ or Eclipse)");
    ideIntegrationConfigurationPanel = new IdeIntegrationConfigurationPanel(otrosApplication);
  }

  @Override
  public JComponent getView() {
    return ideIntegrationConfigurationPanel;
  }

  @Override
  public ValidationResult validate() {
    return new ValidationResult();
  }

  @Override
  public void loadConfiguration(Configuration configuration) {
    ideIntegrationConfigurationPanel.setSelectedHostname(configuration.getString(ConfKeys.JUMP_TO_CODE_HOST,JumpToCodeService.DEFAULT_HOST));
    ideIntegrationConfigurationPanel.setSelectedPort(configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, JumpToCodeService.DEFAULT_PORT));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(ConfKeys.JUMP_TO_CODE_PORT,ideIntegrationConfigurationPanel.getSelectedPort());
    c.setProperty(ConfKeys.JUMP_TO_CODE_HOST,ideIntegrationConfigurationPanel.getSelectedHostname());
  }
}
