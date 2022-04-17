package pl.otros.logview.api.theme;

import java.awt.*;

public interface Theme {

  Type themeType();

  enum Type {
    Dark, Light
  }

  Color getColor(ThemeKey themeKey);

  void setColor(ThemeKey themeKey, Color color);

  void clear(ThemeKey themeKey);

}
