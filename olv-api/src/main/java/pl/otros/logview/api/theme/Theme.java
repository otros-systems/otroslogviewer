package pl.otros.logview.api.theme;

import java.awt.*;

public interface Theme {

  ThemeType themeType();

  Color getColor(ThemeKey themeKey);

  void setColor(ThemeKey themeKey, Color color);

  void clear(ThemeKey themeKey);

}
