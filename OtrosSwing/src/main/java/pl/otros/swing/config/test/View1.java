package pl.otros.swing.config.test;

import org.apache.commons.configuration.Configuration;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;
import java.awt.*;

public class View1 extends AbstractConfigView {
  private JCheckBox jCheckBox;
  private JTextArea textArea;
  private JLabel label;
  private JPanel p;

  public View1() {
    super("view1", "View 1", "View 1 desc sdfs df.as.df d.as f.s");
    label = new JLabel();
    textArea = new JTextArea(4, 20);
    jCheckBox = new JCheckBox("Some checkobx");
    p = new JPanel();
    p.setLayout(new FlowLayout());
    p.add(label);
    p.add(textArea);
    p.add(jCheckBox);
  }

  @Override
  public JComponent getView() {
    return p;
  }

  @Override
  public void loadConfiguration(Configuration configuration) {
    label.setText(configuration.getString("view1.label", "-"));
    textArea.setText(configuration.getString("view1.text", "-"));
    jCheckBox.setSelected(configuration.getBoolean("view1.boolean", false));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty("view1.label", label.getText());
    c.setProperty("view1.text", textArea.getText());
    c.setProperty("view1.boolean", jCheckBox.isSelected());
  }

  public ValidationResult validate() {
    return new ValidationResult();
  }
}
