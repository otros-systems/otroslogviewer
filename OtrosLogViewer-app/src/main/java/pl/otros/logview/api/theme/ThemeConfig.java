package pl.otros.logview.api.theme;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.DataConfiguration;

import javax.swing.*;
import java.awt.*;

public class ThemeConfig implements Theme {
  private DataConfiguration configuration;

  public ThemeConfig(Configuration configuration) {
    this.configuration = new DataConfiguration(configuration);
  }

  @Override
  public Color getColor(ThemeKey themeKey) {
    final Type type = themeType();
    final Color defaultValue = type == Type.Light ? themeKey.getDefaultLightColor() : themeKey.getDefaultDarkColor();
    return configuration.getColor(themeKey.getKey(type), defaultValue);
  }

  @Override
  public void setColor(ThemeKey themeKey, Color c) {
    String k = themeKey.getKey(themeType());
    String color = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    configuration.setProperty(k,color);
  }

  @Override
  public void clear(ThemeKey themeKey) {
    configuration.clearProperty(themeKey.getKey(themeType()));
  }

  @Override
  public Theme.Type themeType() {

    final LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
    final Color color = lookAndFeel.getDefaults().getColor("Label.background");
    if (Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3])[2] > 0.5f) {
      return Type.Light;
    } else {
      return Type.Dark;
    }
  }

}
