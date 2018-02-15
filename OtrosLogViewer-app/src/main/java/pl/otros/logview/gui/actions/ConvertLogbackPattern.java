package pl.otros.logview.gui.actions;

import pl.otros.logview.api.LayoutEncoderConverter;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.logppattern.LogbackLayoutEncoderConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.util.Properties;

public class ConvertLogbackPattern extends OtrosAction {

  public ConvertLogbackPattern(OtrosApplication otrosApplication) {
    super("Convert logback action",otrosApplication);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final String logbackPattern = JOptionPane.showInputDialog("Enter logback pattern");
    try {
      final LayoutEncoderConverter logbackLayoutEncoderConverter = new LogbackLayoutEncoderConverter();
      final Properties convert = logbackLayoutEncoderConverter.convert(logbackPattern);
      final StringWriter writer = new StringWriter();
      convert.store(writer,"");
      final String s = writer.getBuffer().toString();
      final JTextArea jTextArea = new JTextArea(s);
      JOptionPane.showMessageDialog((Component) e.getSource(),jTextArea);
    } catch (Exception e1){
      JOptionPane.showMessageDialog((Component) e.getSource(),"Can't process: " + e1.getMessage());
    }
  }
}
