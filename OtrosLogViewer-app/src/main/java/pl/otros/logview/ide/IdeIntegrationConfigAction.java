package pl.otros.logview.ide;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.actions.OtrosAction;
import pl.otros.logview.gui.services.Services;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.logging.Level;
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
    if (confirmDialog == JOptionPane.YES_OPTION){
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_HOST , StringUtils.defaultIfBlank(ideIntegrationConfigurationPanel.getSelectedHostname(), null));
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_PORT ,ideIntegrationConfigurationPanel.getSelectedPort());
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    showConfigDialog();
  }
}
