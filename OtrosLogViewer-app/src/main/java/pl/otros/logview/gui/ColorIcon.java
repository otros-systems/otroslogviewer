/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
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

  private Color background;
  private Color foreground;
  private int width;
  private int height;
  private char[] chars = new char[] { 'A', 'b' };

  public ColorIcon(Color background, Color foreground) {
    this(background, foreground, 16, 16);
  }

  public ColorIcon(Color background, Color foreground, int width, int height) {
    super();
    this.background = background;
    this.foreground = foreground;
    this.width = width;
    this.height = height;
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
