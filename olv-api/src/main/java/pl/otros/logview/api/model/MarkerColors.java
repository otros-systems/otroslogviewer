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
package pl.otros.logview.api.model;

import java.awt.*;

public enum MarkerColors {

  Red(Color.BLACK, new Color(0xF2, 0x8B, 0x82)),
  Yellow(Color.BLACK, new Color(0xF7, 0xE3, 0x82)),
  Black(Color.WHITE, Color.BLACK),
  White(Color.BLACK, Color.WHITE),

  Green(Color.BLACK, new Color(0x81, 0xC9, 0x95)),
  Lime(Color.BLACK, new Color(0xC5, 0xE4, 0x78)),
  Aqua(Color.BLACK, new Color(0x7D, 0xD9, 0xD2)),
  Pink(Color.BLACK, new Color(0xF3, 0xA6, 0xC8)),

  Purple(Color.BLACK, new Color(0xB3, 0x9D, 0xDB)),
  Brown(Color.BLACK, new Color(0xC8, 0x97, 0x6B)),
  Orange(Color.BLACK, new Color(0xFF, 0xB7, 0x4D));

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
