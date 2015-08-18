package pl.otros.swing.config.test;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatView extends AbstractConfigView {
  private final JPanel p;
  private final JComboBox combobox;

  public DateFormatView() {
    super("DateFormat", "Date format", "Format of the date");
    p = new JPanel();
    p.setLayout(new MigLayout());
    combobox = new JComboBox(new String[]{
        "HH:mm:ss", // 
        "HH:mm:ss.SSS",// 
        "dd-MM HH:mm:ss.SSS",// 
        "E HH:mm:ss", //
        "E HH:mm:ss.SS", //
        "MMM dd, HH:mm:ss",// 
    });
    addLabel("Date format", 'd', combobox);
    final JTextField exampleTextField = new JTextField(20);
    exampleTextField.setEditable(false);
    addLabel("Format example", 'e', exampleTextField);
    combobox.addActionListener(e -> exampleTextField.setText(new SimpleDateFormat((String) combobox.getSelectedItem()).format(new Date())));
  }

  private void addLabel(String string, char c, JComponent jComponent) {
    JLabel label = new JLabel(string);
    p.add(label);
    label.setDisplayedMnemonic(c);
    label.setLabelFor(jComponent);
    p.add(jComponent, "growx, wrap");
  }

  @Override
  public JComponent getView() {
    return p;
  }

  @Override
  public void loadConfiguration(Configuration configuration) {
    String string = configuration.getString("dateformat.df");
    if (!StringUtils.isBlank(string)) {
      combobox.setSelectedItem(string);
    } else {
      combobox.setSelectedIndex(0);
    }
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty("dateformat.df", combobox.getSelectedItem());
  }

  public ValidationResult validate() {
    return new ValidationResult();
  }
}
