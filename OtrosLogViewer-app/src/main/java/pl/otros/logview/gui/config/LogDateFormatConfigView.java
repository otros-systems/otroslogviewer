package pl.otros.logview.gui.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXRadioGroup;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.swing.config.AbstractConfigView;
import pl.otros.swing.config.InMainConfig;
import pl.otros.swing.config.ValidationResult;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import static pl.otros.logview.gui.ConfKeys.*;

public class LogDateFormatConfigView extends AbstractConfigView implements InMainConfig {

  private final String[] dateFormats;
  private final JXRadioGroup radioGroup;
  private JPanel panel;
  private final JXComboBox dateFormatRadio;

  public LogDateFormatConfigView() {
    super("logDisplay", "Log event display", "This configuration provides allow user to change how log events are displayed");
    panel = new JPanel();
    panel.setLayout(new MigLayout());
    dateFormats = new String[]{
        "HH:mm:ss", //
        "HH:mm:ss.SSS",//
        "dd-MM HH:mm:ss.SSS",//
        "E HH:mm:ss", //
        "E HH:mm:ss.SS", //
        "MMM dd, HH:mm:ss",//
    };
    dateFormatRadio = new JXComboBox(dateFormats);
//    dateFormatRadio.setLayoutAxis(BoxLayout.Y_AXIS);
    addLabel("Date format", 'd', dateFormatRadio);
    final JTextField exampleTextField = new JTextField(20);
    exampleTextField.setEditable(false);
    addLabel("Format example", 'e', exampleTextField);
    dateFormatRadio.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        exampleTextField.setText(new SimpleDateFormat(dateFormatRadio.getSelectedItem().toString()).format(new Date()));
      }
    });
    radioGroup = new JXRadioGroup(LevelRenderer.Mode.values());
    addLabel("Level display", 'l', radioGroup);
  }

  private void addLabel(String string, char c, JComponent jComponent) {
    JLabel label = new JLabel(string);
    panel.add(label);
    label.setDisplayedMnemonic(c);
    label.setLabelFor(jComponent);
    panel.add(jComponent, "growx, wrap");
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
  public void loadConfiguration(Configuration configuration) {
    LevelRenderer.Mode mode = LevelRenderer.Mode.valueOf(configuration.getString(LOG_DATA_FORMAT_LEVEL_RENDERER, LevelRenderer.Mode.IconsOnly.name()));
    radioGroup.setSelectedValue(mode);
    String dateFormat = StringUtils.defaultIfBlank(configuration.getString(LOG_DATA_FORMAT_DATE_FORMAT), dateFormats[1]);

    dateFormatRadio.setSelectedItem(dateFormat);
  }

  @Override
  public void saveConfiguration(Configuration c) {
    c.setProperty(LOG_DATA_FORMAT_LEVEL_RENDERER, ((LevelRenderer.Mode) radioGroup.getSelectedValue()).name());
    c.setProperty(LOG_DATA_FORMAT_DATE_FORMAT, radioGroup.getSelectedValue());
  }
}
