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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class GuiUtils {

  public static JMenu sortDirAndAlfabetic(JMenu root) {
    int count = root.getItemCount();
    JMenuItem[] items = new JMenuItem[count];
    for (int i = 0; i < count; i++) {
      items[i] = root.getItem(i);
    }
    Arrays.sort(items, (m1, m2) -> {
      if (m1.getClass().equals(m2.getClass())) {
        return m1.getText().toLowerCase().compareTo(m2.getText().toLowerCase());
      } else if (m1 instanceof JMenu && !(m2 instanceof JMenu)) {
        return -1;
      } else if (m2 instanceof JMenu && !(m1 instanceof JMenu)) {
        return 1;
      }
      return 0;
    });
    root.removeAll();
    for (JMenuItem menuItem : items) {
      if (menuItem instanceof JMenu) {
        menuItem = GuiUtils.sortDirAndAlfabetic((JMenu) menuItem);
      }
      root.add(menuItem);
    }
    return root;
  }

  public static void centerOnComponet(Window target, JComponent parent) {
    Dimension targetSize = target.getSize();
    Point location = parent.getLocationOnScreen();
    Dimension sourceSize = parent.getSize();

    Point sourceCenter = new Point(location.x + sourceSize.width / 2, location.y + sourceSize.height / 2);
    Point frameLocation = new Point(sourceCenter.x - targetSize.width / 2, sourceCenter.y - targetSize.height / 2);
    target.setLocation(frameLocation);
  }

  public static void centerOnScreen(Window target) {
    DisplayMode displayMode = target.getGraphicsConfiguration().getDevice().getDisplayMode();
    int displayHeight = displayMode.getHeight();
    int displayWidth = displayMode.getWidth();

    Dimension targetSize = target.getSize();

    Point frameLocation = new Point(displayWidth / 2 - targetSize.width / 2, displayHeight / 2 - targetSize.height / 2);
    target.setLocation(frameLocation);
  }

  public static void runLaterInEdt(Runnable runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public static void runNowInEdt(Runnable runnable) throws InvocationTargetException, InterruptedException {
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeAndWait(runnable);
    }
  }
}
