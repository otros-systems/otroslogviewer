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
package pl.otros.logview.gui.util;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CompoundIcon implements Icon {

  private final List<Icon> icons;
  private int width;
  private int height;

  public CompoundIcon(List<Icon> icons) {
    super();
    this.icons = icons;
    for (Icon icon : icons) {
      if (icon == null) {
        continue;
      }
      width += icon.getIconWidth();
      height = Math.max(height, icon.getIconHeight());
    }
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    int xPos = x;
    for (Icon icon : icons) {
      icon.paintIcon(c, g, xPos, y);
      xPos += icon.getIconWidth();
    }
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
