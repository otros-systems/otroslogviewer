package pl.otros.logview.gui.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import pl.otros.logview.api.ConfKeys;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;

public class SocketReaderConfigView extends AbstractConfigView implements InMainConfig {


  private final JPanel panel;
  private final SpinnerNumberModel spinnerNumberModel;
  private final JSpinner spinner;

  public SocketReaderConfigView() {
    super("SocketReader", "Socket reader", "Socket reader configuration");
    panel = new JPanel(new MigLayout("left"));
    spinnerNumberModel = new SpinnerNumberModel(100, 100, 10000, 100);
    spinner = new JSpinner(spinnerNumberModel);
    addLabel("Log buffering time in milliseconds", 'b', spinner, panel);
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
    spinnerNumberModel.setValue(c.getInt(ConfKeys.READER_SOCKET_BUFFER_TIME, 1000));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(ConfKeys.READER_SOCKET_BUFFER_TIME, spinnerNumberModel.getValue());
  }
}
