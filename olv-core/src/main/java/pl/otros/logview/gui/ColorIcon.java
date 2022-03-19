/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui;

import javax.swing.*;
import java.awt.*;

public class ColorIcon implements Icon {

  private final Color background;
  private final Color foreground;
  private final int width;
  private final int height;
  private char[] chars = {};

  public ColorIcon(Color background) {
    this(background, background, "");
  }

  public ColorIcon(Color background, Color foreground, String text) {
    this(background, foreground, 16, 16, text);
  }

  public ColorIcon(Color background, Color foreground, int width, int height, String string) {
    super();
    this.background = background;
    this.foreground = foreground;
    this.width = width;
    this.height = height;
    this.chars = string.toCharArray();
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.setColor(background);
    g.fillRect(x, y, width, height);
    g.setColor(foreground);
    g.drawChars(chars, 0, chars.length, 2, getIconHeight() - 2);
  }

  @Override
  public int getIconWidth() {
    return width;
  }

  @Override
  public int getIconHeight() {

    return height;
  }

}
