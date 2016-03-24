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

import pl.otros.logview.VersionUtil;
import pl.otros.logview.api.StatusObserver;

import java.awt.*;
import java.io.IOException;

public class OtrosSplash {

  private static String message;
  private static StatusObserver splashStatusObserver;
  private static String version;

  private static void render() {
    SplashScreen splashScreen = SplashScreen.getSplashScreen();
    if (splashScreen == null) {
      return;
    }
    Graphics2D g = splashScreen.createGraphics();
    if (g == null) {
      return;
    }

    if (version == null) {
      try {
        version = VersionUtil.getRunningVersion();
      } catch (IOException e) {
        version = "?";
      }
      version = "Version: " + version;
    }

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setComposite(AlphaComposite.Clear);
    Rectangle bounds = splashScreen.getBounds();
    g.fillRect(0, 0, bounds.width, bounds.height);
    g.setPaintMode();
    g.setColor(Color.BLACK);
    g.setFont(g.getFont().deriveFont(14f));
    g.drawString(message, 20, 110);
    g.drawString(version, 20, 130);
    splashScreen.update();
  }

  public static void hide() {
    SplashScreen splashScreen = SplashScreen.getSplashScreen();
    if (splashScreen != null) {
      splashScreen.close();
    }
  }

  public static void setMessage(String message) {
    OtrosSplash.message = message;
    render();
  }

  public static StatusObserver getSplashStatusObserver() {
    if (splashStatusObserver == null) {
      splashStatusObserver = new StatusObserver() {

        @Override
        public void updateStatus(String text, int level) {
          updateStatus(text);
        }

        @Override
        public void updateStatus(String text) {
          OtrosSplash.setMessage(text);

        }
      };
    }
    return splashStatusObserver;
  }

}
