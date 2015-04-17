package pl.otros.logview.ide;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.DataConfiguration;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.services.Services;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdeIntegrationConfigurationPanel extends JXPanel {
  private static final Logger LOGGER = Logger.getLogger(IdeIntegrationConfigurationPanel.class.getName());
  private SpinnerNumberModel spinnerNumberModel;
  private JXTextField hostNameTextField;
  private JCheckBox enableAutoJumping;
  private JCheckBox enabledJumpFromStarckTraceCBox;

  public IdeIntegrationConfigurationPanel(final OtrosApplication otrosApplication) {
    DataConfiguration configuration = otrosApplication.getConfiguration();
    setLayout(new MigLayout("left"));

    enabledJumpFromStarckTraceCBox = new JCheckBox("");
    enabledJumpFromStarckTraceCBox.setSelected(configuration.getBoolean(ConfKeys.JUMP_TO_CODE_ENABLED, true));
    JLabel enabledLabel = new JLabel("Enabled jump from stack trace");
    enabledLabel.setLabelFor(enabledJumpFromStarckTraceCBox);
    enabledLabel.setDisplayedMnemonic('e');

    hostNameTextField = new JXTextField("Enter host name");
    hostNameTextField.setText(configuration.getString(ConfKeys.JUMP_TO_CODE_HOST, JumpToCodeService.DEFAULT_HOST));
    JLabel hostLabel = new JLabel("Host:");
    hostLabel.setDisplayedMnemonic('h');
    hostLabel.setLabelFor(hostNameTextField);
    JLabel portLabel = new JLabel("Port:");
    spinnerNumberModel = new SpinnerNumberModel(configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, JumpToCodeService.DEFAULT_PORT), 1025, 65000, 1);
    JSpinner portSpinner = new JSpinner(spinnerNumberModel);
    portLabel.setDisplayedMnemonic('p');
    portLabel.setLabelFor(portSpinner);
    final JXButton testConnectivityButton = new JXButton("Test connectivity", Icons.STATUS_UNKNOWN);
    testConnectivityButton.setMnemonic('t');
    testConnectivityButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Services services = otrosApplication.getServices();
        JumpToCodeService jumpToCodeService = services.getJumpToCodeService();
        boolean ideAvailable = jumpToCodeService.isIdeAvailable(hostNameTextField.getText(), spinnerNumberModel.getNumber().intValue());
        if (ideAvailable) {
          testConnectivityButton.setIcon(Icons.STATUS_OK);
        } else {
          testConnectivityButton.setIcon(Icons.STATUS_ERROR);
        }
      }
    });

    enableAutoJumping = new JCheckBox();
    enableAutoJumping.setSelected(configuration.getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, true));
    JLabel enableAutoJumpingLabel = new JLabel("Enable autojump", Icons.ARROW_STEP_OVER, SwingConstants.RIGHT);
    enableAutoJumpingLabel.setLabelFor(enableAutoJumping);
    enableAutoJumpingLabel.setDisplayedMnemonic('a');

    JXHyperlink setDefaults = new JXHyperlink(new AbstractAction("Set defaults") {
      @Override
      public void actionPerformed(ActionEvent e) {
        spinnerNumberModel.setValue(JumpToCodeService.DEFAULT_PORT);
        hostNameTextField.setText(JumpToCodeService.DEFAULT_HOST);
      }
    });
    setDefaults.setMnemonic('d');
    JXHyperlink openHelp = new JXHyperlink(new AbstractAction("Open help") {
      @Override
      public void actionPerformed(ActionEvent e) {
        ListeningScheduledExecutorService listeningScheduledExecutorService = otrosApplication.getServices().getTaskSchedulerService().getListeningScheduledExecutorService();
        listeningScheduledExecutorService.submit(new Runnable() {
          @Override
          public void run() {
            try {
              Desktop.getDesktop().browse(new URI("https://github.com/otros-systems/otroslogviewer/wiki/JumpToCode"));
            } catch (Exception e1) {
              LOGGER.log(Level.SEVERE, "Can't open page", e1);
            }
          }
        });
      }
    });
    openHelp.setMnemonic('o');
    add(hostLabel, "alignx trailing");
    add(hostNameTextField, "wrap, growx");
    add(portLabel, "alignx trailing");
    add(portSpinner, "wrap,growx");
    add(testConnectivityButton, "span, center, growx");
    add(enabledLabel, "alignx, trailing");
    add(enabledJumpFromStarckTraceCBox, "span, left,wrap");
    add(enableAutoJumpingLabel, "alignx trailing");
    add(enableAutoJumping, "span,left, wrap");
    add(setDefaults, "left, span, growx");
    add(openHelp, "span, left, wrap");
  }

  public String getSelectedHostname() {
    return hostNameTextField.getText();
  }

  public void setSelectedHostname(String hostname) {
    hostNameTextField.setText(hostname);
  }

  public int getSelectedPort() {
    return spinnerNumberModel.getNumber().intValue();
  }

  public void setSelectedPort(int port) {
    spinnerNumberModel.setValue(Integer.valueOf(port));
  }

  public boolean isAutoJumpEnabled() {
    return enableAutoJumping.isSelected();
  }

  public void setAutoJumpEnabled(boolean enabled) {
    enableAutoJumping.setSelected(enabled);
  }

  public boolean isEnabledIdeIntegration() {
    return enabledJumpFromStarckTraceCBox.isSelected();
  }

  public void setEnabledIdeIntegration(boolean enabled) {
    enabledJumpFromStarckTraceCBox.setSelected(enabled);
  }

}
