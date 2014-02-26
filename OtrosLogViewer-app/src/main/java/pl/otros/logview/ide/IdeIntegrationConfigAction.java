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

    JXPanel jxPanel = new JXPanel();
    jxPanel.setLayout(new MigLayout("center"));
    final JXTextField hostNameTextField = new JXTextField("Enter host name");
    hostNameTextField.setText(configuration.getString(ConfKeys.JUMP_TO_CODE_HOST,JumpToCodeService.DEFAULT_HOST));

    JLabel hostLabel = new JLabel("Host:");
    hostLabel.setDisplayedMnemonic('h');
    hostLabel.setLabelFor(hostNameTextField);

    JLabel portLabel = new JLabel("Port:");
    final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, JumpToCodeService.DEFAULT_PORT), 1025, 65000, 1);
    JSpinner portSpinner = new JSpinner(spinnerNumberModel);
    portLabel.setDisplayedMnemonic('p');
    portLabel.setLabelFor(portSpinner);

    final JXButton testConnectivityButton = new JXButton("Test connectivity",Icons.STATUS_UNKNOWN);
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
              Desktop.getDesktop().browse(new URI("http://code.google.com/p/otroslogviewer/wiki/JumpToCode"));
            } catch (Exception e1) {
              LOGGER.log(Level.SEVERE, "Can't open page", e1);
            }
          }
        });
      }
    });
    openHelp.setMnemonic('o');


    jxPanel.add(hostLabel, "alignx trailing");
    jxPanel.add(hostNameTextField, "wrap, growx");
    jxPanel.add(portLabel, "alignx trailing");
    jxPanel.add(portSpinner, "wrap,growx");

    jxPanel.add(testConnectivityButton,"span, center, growx");

    jxPanel.add(setDefaults,"left, span, growx");

    jxPanel.add(openHelp,"span, left, wrap");



    int confirmDialog = JOptionPane.showConfirmDialog(null, jxPanel, "IDE integration configuration", JOptionPane.OK_CANCEL_OPTION);
    if (confirmDialog == JOptionPane.YES_OPTION){
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_HOST , StringUtils.defaultIfBlank(hostNameTextField.getText(), null));
      configuration.setProperty(ConfKeys.JUMP_TO_CODE_PORT ,spinnerNumberModel.getNumber());
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    showConfigDialog();
  }
}
