package pl.otros.logview.ide;

import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.actions.OtrosAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 */
public class IdeIntegrationConfigAction extends OtrosAction {
  private static final Logger LOGGER = Logger.getLogger(IdeIntegrationConfigAction.class.getName());

  public IdeIntegrationConfigAction(OtrosApplication otrosApplication) {
    super(otrosApplication);
  }


  private void showConfigDialog() {
    final OtrosApplication otrosApplication = getOtrosApplication();
    final DataConfiguration configuration = otrosApplication.getConfiguration();

    IdeIntegrationConfigurationPanel ideIntegrationConfigurationPanel = new IdeIntegrationConfigurationPanel(getOtrosApplication());

    int confirmDialog = JOptionPane.showConfirmDialog(otrosApplication.getApplicationJFrame(), ideIntegrationConfigurationPanel, "IDE integration configuration", JOptionPane.OK_CANCEL_OPTION);
    if (confirmDialog == JOptionPane.YES_OPTION) {
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_HOST, StringUtils.defaultIfBlank(ideIntegrationConfigurationPanel.getSelectedHostname(), null));
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_PORT, ideIntegrationConfigurationPanel.getSelectedPort());
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, ideIntegrationConfigurationPanel.isAutoJumpEnabled());
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_ENABLED, ideIntegrationConfigurationPanel.isEnabledIdeIntegration());
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    showConfigDialog();
  }
}
