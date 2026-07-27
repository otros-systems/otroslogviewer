package pl.otros.logview.gui.actions;

import org.apache.commons.lang3.Strings;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class FontSize extends OtrosAction {
  public static final int DEFAULT_FONT_SIZE = 12;

  private final int fontSize;

  public FontSize(OtrosApplication otrosApplication, int fontSize) {
    super("Set font size", otrosApplication);
    this.fontSize = fontSize;
  }


  @Override
  protected void actionPerformedHook(ActionEvent e) {
    setDefaultSize(fontSize);
    Arrays.asList(JFrame.getFrames()).forEach(SwingUtilities::updateComponentTreeUI);
  }

  public static void setDefaultSize(int size) {

    UIManager.getLookAndFeelDefaults().keySet().stream()
      .filter(key -> Strings.CI.contains(key.toString(), "font"))
      .forEach(key -> {
        Font font = UIManager.getDefaults().getFont(key);
        if (font != null) {
          //the defaults are bold, but plain needed.
          font = font.deriveFont((float) size).deriveFont(Font.PLAIN);
          //The new LookAndFeel Manager needs FontUIResource
          FontUIResource fontResource = new FontUIResource(font.deriveFont(Font.PLAIN, size));
          UIManager.put(key, fontResource);
        }
      });
  }
}
