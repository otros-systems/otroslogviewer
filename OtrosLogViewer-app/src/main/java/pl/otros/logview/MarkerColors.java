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
package pl.otros.logview;

import java.awt.*;

public enum MarkerColors {

  Red(Color.WHITE, new Color(0xff, 0x2b, 0x2b)), Yellow(Color.BLACK, Color.YELLOW), Black(Color.WHITE, Color.BLACK), White(Color.BLACK, Color.WHITE), Green(
      Color.WHITE, new Color(0x11, 0x66, 0)), Lime(Color.BLACK, new Color(0x22, 255, 0)), Aqua(Color.BLACK, new Color(0, 255, 255)), Pink(Color.BLACK,
      Color.PINK), Purple(Color.WHITE, new Color(0x80, 0, 0x80)), Brown(Color.WHITE, new Color(150, 75, 0)), Orange(Color.BLACK, Color.ORANGE);

  private final Color background;
  private final Color foreground;

  MarkerColors(Color foreground, Color background) {
    this.background = background;
    this.foreground = foreground;

  }

  public Color getBackground() {
    return background;
  }

  public Color getForeground() {
    return foreground;
  }

  public static MarkerColors fromString(String name) {
    MarkerColors valueOf = null;
    try {
      valueOf = MarkerColors.valueOf(MarkerColors.class, name);
    } catch (Exception e) {

      valueOf = MarkerColors.Aqua;
    }
    return valueOf;
  }

}
