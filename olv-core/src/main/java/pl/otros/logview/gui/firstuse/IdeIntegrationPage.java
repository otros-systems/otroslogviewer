package pl.otros.logview.gui.firstuse;

import com.github.cjwizard.WizardPage;
import com.github.cjwizard.WizardSettings;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.DataConfiguration;
import org.jdesktop.swingx.JXHyperlink;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.Ide;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.services.JumpToCodeService;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeServiceImpl;
import pl.otros.swing.functional.DocumentChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class IdeIntegrationPage extends WizardPage {

  private static final String WIKI_ECLIPSE = "https://github.com/otros-systems/otroslogviewer/wiki/JumpToCode#how-to-install-integration-with-eclipse";
  private static final String WIKI_IDEA = "https://github.com/otros-systems/otroslogviewer/wiki/JumpToCode#how-to-install-integration-with-intellij-idea";
  private final DataConfiguration dataConfiguration;
  private final JTextField host;
  private final SpinnerNumberModel model;
  private Ide ide = Ide.DISCONNECTED;
  private final JumpToCodeServiceImpl jumpToCodeService;
  private final JButton testIdeIntegration;
  private final JLabel statusLabel;

  IdeIntegrationPage() {
    super("Ide integration", "");
    host = new JTextField("localhost");
    dataConfiguration = new DataConfiguration(new BaseConfiguration());
    host.getDocument().addDocumentListener(new DocumentChangeListener(() -> dataConfiguration.setProperty(ConfKeys.JUMP_TO_CODE_HOST, host.getText())));
    model = new SpinnerNumberModel(JumpToCodeService.DEFAULT_PORT.intValue(), 1, 1024 * 64 - 1, 1);
    model.addChangeListener(e -> dataConfiguration.setProperty(ConfKeys.JUMP_TO_CODE_PORT, model.getValue()));

    final JXHyperlink hyperlinkEclipse = new JXHyperlink(new AbstractAction("I'm using Eclipse") {
      @Override
      public void actionPerformed(ActionEvent e) {
        openUrlInBrowser(WIKI_ECLIPSE);
      }
    });
    hyperlinkEclipse.setForeground(new Color(0x8888FF));
    final JXHyperlink hyperlinkIntellij = new JXHyperlink(new AbstractAction("I'm using Intellij IDEA") {
      @Override
      public void actionPerformed(ActionEvent e) {
        openUrlInBrowser(WIKI_IDEA);
      }
    });
    hyperlinkIntellij.setForeground(new Color(0x8888FF));
    jumpToCodeService = new JumpToCodeServiceImpl(dataConfiguration);
    testIdeIntegration = new JButton("Test IDE integration", Icons.STATUS_UNKNOWN);
    testIdeIntegration.addActionListener((e) -> testIde());
    statusLabel = new JLabel("Ide integration not tested", Icons.STATUS_UNKNOWN, SwingConstants.LEFT);

    setLayout(new MigLayout("center"));
    add(hyperlinkEclipse, "span,wrap");
    add(hyperlinkIntellij, "span,wrap");
    add(new JLabel("Host:"), "right");
    add(host, "growx, wrap");
    add(new JLabel("Port:"), "right");
    add(new JSpinner(model), "wrap");
    add(testIdeIntegration, "span, center, growx");
    add(statusLabel, "wrap");
  }

  private void openUrlInBrowser(String url) {
    try {
      Desktop.getDesktop().browse(new URI(url));
    } catch (IOException | URISyntaxException e1) {
      JOptionPane.showMessageDialog(IdeIntegrationPage.this, "Can't open browser: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void testIde() {
    testIdeIntegration.setEnabled(false);
    testIdeIntegration.setIcon(Icons.STATUS_UNKNOWN);
    new SwingWorker<Optional<Ide>, Void>() {
      @Override
      protected Optional<Ide> doInBackground() {
        return Optional.ofNullable(jumpToCodeService.getIde());
      }

      @Override
      protected void done() {
        testIdeIntegration.setEnabled(true);
        try {
          final Optional<Ide> maybeIde = get();
          testIdeIntegration.setIcon(maybeIde.map(Ide::getIconConnected).orElse(Icons.STATUS_ERROR));
          ide = maybeIde.orElse(Ide.DISCONNECTED);
          if (ide == Ide.DISCONNECTED){
            statusLabel.setText("Cannot connect to IDE");
            statusLabel.setIcon(Icons.STATUS_ERROR);
          } else {
            statusLabel.setText("Ide integration is working!");
            statusLabel.setIcon(Icons.STATUS_OK);
          }
        } catch (InterruptedException | ExecutionException ignore) {
          testIdeIntegration.setIcon(Icons.STATUS_ERROR);
          statusLabel.setIcon(Icons.STATUS_ERROR);
          statusLabel.setText("Cannot connect to IDE");
        }
      }
    }.execute();
  }

  @Override
  public void rendering(List<WizardPage> path, WizardSettings settings) {
    super.rendering(path, settings);
    testIde();
  }

  @Override
  public boolean onNext(WizardSettings settings) {
    settings.put(Config.IDE_HOST, host.getText());
    settings.put(Config.IDE_PORT, model.getValue());
    settings.put(Config.IDE, ide);
    return true;
  }
}
