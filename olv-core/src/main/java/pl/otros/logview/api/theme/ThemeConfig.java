package pl.otros.logview.api.theme;


import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.DataConfiguration;

import javax.swing.*;
import java.awt.*;

public class ThemeConfig implements Theme {
  private DataConfiguration configuration;
  private UIDefaults lookAndFeelDefaults;

  public ThemeConfig(Configuration configuration) {
    this.configuration = new DataConfiguration(configuration);
    final LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
    lookAndFeelDefaults = lookAndFeel.getDefaults();
  }

  @Override
  public Color getColor(ThemeKey themeKey) {
    final ThemeType type = themeType();
    final Color defaultValue = type == ThemeType.Light ? themeKey.getDefaultLightColor() : themeKey.getDefaultDarkColor();
    return configuration.getColor(themeKey.getKey(type), defaultValue);
  }

  @Override
  public void setColor(ThemeKey themeKey, Color c) {
    String k = themeKey.getKey(themeType());
    String color = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    configuration.setProperty(k, color);
  }

  @Override
  public void clear(ThemeKey themeKey) {
    configuration.clearProperty(themeKey.getKey(themeType()));
  }

  @Override
  public ThemeType themeType() {
    final Color color = lookAndFeelDefaults.getColor("Label.background");
    if (Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3])[2] > 0.5f) {
      return ThemeType.Light;
    } else {
      return ThemeType.Dark;
    }
  }

}
