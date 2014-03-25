package pl.otros.swing.config.test;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.jdesktop.swingx.prompt.PromptSupport;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;

public class ValidationView2 extends AbstractConfigView {
  private static final String VALIDATION_NAME = "validation.name";
  private JPanel jPanel;
  private JTextField tfName;
  private JTextField tfAge;

  public ValidationView2() {
    super("validView", "Validation view 2", "View with validation,");
    jPanel = new JPanel(new MigLayout());
    tfName = new JTextField(20);
    PromptSupport.setPrompt("Enter name with uppercase", tfName);
    JLabel labelName = new JLabel("Name: ");
    labelName.setDisplayedMnemonic('m');
    labelName.setLabelFor(tfName);
    tfAge = new JTextField(20);
    PromptSupport.setPrompt("Enter age", tfAge);
    JLabel ageLabel = new JLabel("Age: ");
    ageLabel.setDisplayedMnemonic('a');
    ageLabel.setLabelFor(tfAge);
    jPanel.add(labelName);
    jPanel.add(tfName, "wrap");
    jPanel.add(ageLabel);
    jPanel.add(tfAge, "wrap");
  }

  @Override
  public JComponent getView() {
    return jPanel;
  }

  public ValidationResult validate() {
    ValidationResult r = new ValidationResult();
    String text = tfName.getText();
    if (text.length() >= 0 && !Character.isUpperCase(text.charAt(0))) {
      r.addErrorMessage("Name have to start with uppercase");
    }
    String ageText = tfAge.getText();
    if (!ageText.matches("\\d+")) {
      r.addErrorMessage("Age have to be a integer number");
    }
    return r;
  }

  @Override
  public void loadConfiguration(Configuration configuration) {
    tfName.setText(configuration.getString(VALIDATION_NAME, ""));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(VALIDATION_NAME, tfName.getText());
  }
}
