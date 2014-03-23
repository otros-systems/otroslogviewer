package pl.otros.swing.config.test;

import org.apache.commons.configuration.Configuration;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;
import java.awt.*;

public class View2 extends AbstractConfigView {
  private JCheckBox jCheckBox;
  private JTextArea textArea;
  private JLabel label;
  private JPanel p;

  public View2() {
    super("view2", "View 2", "View 2 desc sdfs df.as.df d.as f.s");
    label = new JLabel();
    textArea = new JTextArea(4, 20);
    jCheckBox = new JCheckBox("Some dfs");
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
  public void loadConfguration(Configuration configuration) {
    label.setText(configuration.getString("view2.label", "-"));
    textArea.setText(configuration.getString("view2.text", "-"));
    jCheckBox.setSelected(configuration.getBoolean("view2.boolean", false));
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty("view2.label", label.getText());
    c.setProperty("view2.text", textArea.getText());
    c.setProperty("view2.boolean", jCheckBox.isSelected());
  }

  public ValidationResult validate() {
    return new ValidationResult();
  }
}
