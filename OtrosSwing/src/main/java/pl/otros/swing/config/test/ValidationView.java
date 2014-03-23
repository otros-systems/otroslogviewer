package pl.otros.swing.config.test;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.jdesktop.swingx.prompt.PromptSupport;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;

public class ValidationView extends AbstractConfigView {
  private static final String VALIDATION_NAME = "validation.name";
  private JPanel jPanel;
  private JTextField tf;

  public ValidationView() {
    super("validView", "Validation view", "View with validation,");
    jPanel = new JPanel(new MigLayout());
    tf = new JTextField(20);
    PromptSupport.setPrompt("Enter name with uppercase", tf);
    JLabel jLabel = new JLabel("Name: ");
    jLabel.setDisplayedMnemonic('m');
    jLabel.setLabelFor(tf);
    jPanel.add(jLabel);
    jPanel.add(tf);
  }

  @Override
  public JComponent getView() {
    return jPanel;
  }

  public ValidationResult validate() {
    String text = tf.getText();
    ValidationResult r = new ValidationResult();
    if (text.length() > 0 && !Character.isUpperCase(text.charAt(0))) {
      r.addErrorMessage("Name have to start with uppercase");
    }
    return r;
  }

  @Override
  public void loadConfguration(Configuration configuration) {
    tf.setText(configuration.getString(VALIDATION_NAME, ""));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(VALIDATION_NAME, tf.getText());
  }
}
