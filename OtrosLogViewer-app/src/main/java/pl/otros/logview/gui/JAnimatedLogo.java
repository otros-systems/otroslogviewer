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

import org.pushingpixels.trident.Timeline.RepeatBehavior;
import org.pushingpixels.trident.ease.Sine;
import org.pushingpixels.trident.swing.SwingRepaintTimeline;

import javax.swing.*;
import java.awt.*;

public class JAnimatedLogo extends JLabel {

  private double alpha;
  private SwingRepaintTimeline timeLine;

  public JAnimatedLogo(String text, int horizontalAlignment) {
    super(text, horizontalAlignment);
    createTimeLine();
  }

  public JAnimatedLogo(String text) {
    super(text);
    createTimeLine();
  }

  public JAnimatedLogo() {
    super();
    createTimeLine();

  }

  private void createTimeLine() {
    LogoIcon icon = new LogoIcon();
    this.setIcon(icon);

    timeLine = new SwingRepaintTimeline(this);
    timeLine.addPropertyToInterpolate("alpha", 0, 45);
    timeLine.setDuration(800);
    timeLine.setEase(new Sine());
  }

  class LogoIcon implements Icon {

    private final int size = 23;
    private final int inset = 2;

    @Override
    public int getIconHeight() {
      return 64;
    }

    @Override
    public int getIconWidth() {
      return (int) (inset * 3 + size + Math.sqrt(3 * size * size));
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      paintConstants(graphics, x, y);
      paintVary(graphics);
    }

    private void paintVary(Graphics graphics) {
      Graphics2D g2 = (Graphics2D) graphics;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Point upLeft = new Point((int) (Math.sqrt(size * size * 2) * Math.sin(Math.PI * (alpha - 45) / 180)),
          (int) (-Math.sqrt(size * size * 2) * Math.cos(Math.PI * (alpha - 45) / 180)));
      Point upRight = new Point((int) (size * Math.sin(Math.PI * alpha / 180)), (int) (-size * Math.cos(Math.PI * alpha / 180)));
      Point downLeft = new Point(-(int) (size * Math.cos(Math.PI * alpha / 180)), -(int) (size * Math.sin(Math.PI * alpha / 180)));
      Point downRight = new Point(0, 0);

      // move

      move(upLeft);
      move(upRight);
      move(downRight);
      move(downLeft);
      graphics.setColor(Color.WHITE);
      graphics.fillPolygon(new int[] { upLeft.x, upRight.x, downRight.x, downLeft.x }, new int[] { upLeft.y, upRight.y, downRight.y, downLeft.y }, 4);
      graphics.setColor(Color.BLACK);
      graphics.drawPolygon(new int[] { upLeft.x, upRight.x, downRight.x, downLeft.x }, new int[] { upLeft.y, upRight.y, downRight.y, downLeft.y }, 4);
    }

    private void move(Point p) {
      p.x = p.x + 2 * size + 2 * inset;
      p.y = p.y + 64 - inset - size - inset;
    }

    private void paintConstants(Graphics g, int x, int y) {
      // Bottom left
      g.setColor(Color.BLACK);
      g.fillRect(inset, 64 - inset - size, size, size);
      g.setColor(Color.GREEN);
      g.fillRect(inset + 1, 64 + 1 - inset - size, size - 2, size - 2);

      // bottom, left
      g.setColor(Color.BLACK);
      g.fillRect(inset, 64 - inset * 2 - 2 * size, size, size);
      g.setColor(Color.BLUE);
      g.fillRect(inset + 1, 64 - inset * 2 - 2 * size + 1, size - 2, size - 2);

      // bottom right t
      g.setColor(Color.BLACK);
      g.fillRect(inset + size + inset, 64 - inset - size, size, size);
      g.setColor(Color.RED);
      g.fillRect(inset + size + inset + 1, 64 + 1 - inset - size, size - 2, size - 2);
    }

  }

  public double getAlpha() {
    return alpha;
  }

  public void setAlpha(double alpha) {
    this.alpha = alpha;

  }

  public void start() {
    timeLine.playLoop(RepeatBehavior.REVERSE);
  }

  public void stop() {
    timeLine.cancelAtCycleBreak();
  }

}
