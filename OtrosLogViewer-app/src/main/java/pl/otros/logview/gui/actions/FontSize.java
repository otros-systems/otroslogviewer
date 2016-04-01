package pl.otros.logview.gui.actions;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class FontSize extends OtrosAction {

  private final int fontSize;

  public FontSize(OtrosApplication otrosApplication, int fontSize) {
    super("Set font size", otrosApplication);
    this.fontSize = fontSize;
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    setDefaultSize(fontSize);
    Arrays.asList(JFrame.getFrames()).forEach(SwingUtilities::updateComponentTreeUI);
  }

  public static void setDefaultSize(int size) {

    UIManager.getLookAndFeelDefaults().keySet().stream()
      .filter(key -> StringUtils.containsIgnoreCase(key.toString(), "font"))
      .forEach(key -> {
        Font font = UIManager.getDefaults().getFont(key);
        if (font != null) {
          font = font.deriveFont((float) size);
          UIManager.put(key, font);
        }
      });
  }
}
