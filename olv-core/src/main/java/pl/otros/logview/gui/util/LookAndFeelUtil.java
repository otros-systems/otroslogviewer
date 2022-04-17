package pl.otros.logview.gui.util;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.plaf.AbstractComponentAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.OtrosSplash;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class LookAndFeelUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(LookAndFeelUtil.class.getName());

  public static void initLf(String lookAndFeel) {
    try {
      OtrosSplash.setMessage("Loading L&F " + lookAndFeel);
      LOGGER.debug("Initializing look and feel: " + lookAndFeel);
      LookAndFeelAddons.contribute(new Addon());
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Throwable e1) {
      LOGGER.warn("Cannot initialize LookAndFeel: " + e1.getMessage());
    }
  }

  private static class Addon extends AbstractComponentAddon {
    Addon() {
      super("OlvHyperlink");
    }

    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
      super.addBasicDefaults(addon, defaults);
      defaults.add(JXHyperlink.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicHyperlinkUI");
      //register color that works for light and dark theme
      defaults.add("Hyperlink.linkColor", new ColorUIResource(80, 0x80, 0xFF));
    }
  }
}
