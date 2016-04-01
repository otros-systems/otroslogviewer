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

public class LifelinePainter implements ExtraPainter {

  private final int[] xPositions;
  private final Color color;

  public LifelinePainter(int[] positions, Color color) {
    super();
    xPositions = positions;
    this.color = color;
  }

  @Override
  public void paint(Graphics g, Rectangle view) {
    g.setColor(color);
    for (int xPosition : xPositions) {
      if (xPosition > view.x && xPosition < view.x + view.width) {
        double y = view.getY();
        while (y < view.getY() + view.getHeight()) {
          g.drawLine(xPosition, (int) y, xPosition, (int) y + 5);
          y += 10;
        }

      }
    }

  }

}
