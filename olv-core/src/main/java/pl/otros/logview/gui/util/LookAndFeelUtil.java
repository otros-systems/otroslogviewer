package pl.otros.logview.gui.util;

import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.plaf.AbstractComponentAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.pushingpixels.radiance.theming.api.skin.RadianceBusinessLookAndFeel;
import org.pushingpixels.radiance.theming.api.skin.RadianceGraphiteAquaLookAndFeel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.OtrosSplash;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.util.List;
import java.util.Objects;

public final class LookAndFeelUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(LookAndFeelUtil.class.getName());
  private static final BasicLookAndFeel LIGHT_LOOK_AND_FEEL = new RadianceBusinessLookAndFeel();
  private static final BasicLookAndFeel DARK_LOOK_AND_FEEL = new RadianceGraphiteAquaLookAndFeel();

  private LookAndFeelUtil() {
    /* This utility class should not be instantiated */
  }

  public static void initLf(String lookAndFeel) {
    try {
      OtrosSplash.setMessage("Loading L&F " + lookAndFeel);
      LOGGER.debug("Initializing look and feel: {}", lookAndFeel);
      LookAndFeelAddons.contribute(new Addon());
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (RuntimeException | ClassNotFoundException | InstantiationException | IllegalAccessException |
             UnsupportedLookAndFeelException e1) {
      LOGGER.warn("Cannot initialize LookAndFeel: {}", e1.getMessage());
    }
  }

  public static BasicLookAndFeel getLightLookAndFeel() {
    return LIGHT_LOOK_AND_FEEL;
  }

  public static BasicLookAndFeel getDarkLookAndFeel() {
    return DARK_LOOK_AND_FEEL;
  }

  public static List<BasicLookAndFeel> getSupportedLookAndFeels() {
    return List.of(LIGHT_LOOK_AND_FEEL, DARK_LOOK_AND_FEEL);
  }

  public static String checkSupportedLookAndFeelOrReturnDefault(String lookAndFeelClassname) {
    if (isSupportedLookAndFeel(lookAndFeelClassname) || Objects.equals(UIManager.getSystemLookAndFeelClassName(), lookAndFeelClassname)) {
      return lookAndFeelClassname;
    }
    return getLightLookAndFeel().getClass().getName();
  }

  public static boolean isSupportedLookAndFeel(String lookAndFeelClassname) {
    return getSupportedLookAndFeels().stream()
      .map(BasicLookAndFeel::getClass)
      .map(Class::getName)
      .anyMatch(name -> Objects.equals(name, lookAndFeelClassname));
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
