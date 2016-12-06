/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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
 */

package pl.otros.vfs.browser.util;

import org.pushingpixels.trident.Timeline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class GuiUtils {
  public static Color getAverageColor(Color color1, Color color2) {
    return new Color(color1.getRed() / 2 + color2.getRed() / 2, color1.getGreen() / 2 + color2.getGreen() / 2, color1.getBlue() / 2 + color2.getBlue() / 2);
  }

  public static void addBlinkOnFocusGain(final JComponent component){
    component.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        blinkComponent(component);
      }

      @Override
      public void focusLost(FocusEvent e) {
      }
    });
  }

  public static void blinkComponent(JComponent component){
    final Timeline timeline = new Timeline(component);
    timeline.addPropertyToInterpolate("background", component.getBackground(), GuiUtils.getAverageColor(component.getBackground(), component.getForeground()));
    timeline.setDuration(150);
    timeline.playLoop(2, Timeline.RepeatBehavior.REVERSE);
  }
}
