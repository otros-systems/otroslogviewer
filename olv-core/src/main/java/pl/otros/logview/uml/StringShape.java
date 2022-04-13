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
package pl.otros.logview.uml;

import java.awt.*;

public class StringShape {

  private String string;
  private Font font;
  private final int centerX;
  private final int centerY;
  private final Color color;

  public StringShape(String text, Font font, int centerX, int centerY, Color color) {
    super();
    if (text.length() == 0) {
      text = " ";
    }
    this.string = text;
    this.font = font;
    this.centerX = centerX;
    this.centerY = centerY;
    this.color = color;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    if (string.length() == 0) {
      string = " ";
    }
    this.string = string;
  }

  public Font getFont() {
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }

  public Rectangle getRectangle(FontMetrics fm) {
    int height = fm.getHeight();
    int width = fm.stringWidth(string);
    Rectangle r = new Rectangle(centerX - width / 2, centerY - height / 2, centerX + width / 2, centerY / width / 2);
    return r;
  }

  public Color getColor() {
    return color;
  }
}
