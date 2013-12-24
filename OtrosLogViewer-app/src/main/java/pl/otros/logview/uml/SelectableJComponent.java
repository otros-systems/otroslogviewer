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
package pl.otros.logview.uml;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class SelectableJComponent extends JComponent {

  private ArrayList<ShapeWithColor> shapesWithColor;
  private ArrayList<StringShape> stringList;

  private LinkedList<ExtraPainter> extraPaintersBefore;

  public SelectableJComponent() {
    shapesWithColor = new ArrayList<ShapeWithColor>();
    stringList = new ArrayList<StringShape>();
    extraPaintersBefore = new LinkedList<ExtraPainter>();
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    Rectangle view = new Rectangle();
    if (getParent() instanceof JViewport) {
      JViewport vp = (JViewport) getParent();
      view = vp.getViewRect();
    } else {
      view = new Rectangle(0, 0, getWidth(), getHeight());
    }

    g2d.setColor(getBackground());
    g2d.fillRect((int) view.getX(), (int) view.getY(), (int) view.getWidth(), (int) view.getHeight());

    for (ExtraPainter ep : extraPaintersBefore) {
      ep.paint(g2d, view);
    }
    g2d.setColor(Color.YELLOW);
    double x = view.getX();
    double y = view.getY();
    double w = view.getWidth();
    double h = view.getHeight();

    for (ShapeWithColor shapeWithColor : shapesWithColor) {
      Rectangle sb = shapeWithColor.getShape().getBounds();
      if (containShape(view, sb)) {
        g2d.setColor(shapeWithColor.getColor());
        if (shapeWithColor.isFill()) {
          g2d.fill(shapeWithColor.getShape());
        } else {
          g2d.draw(shapeWithColor.getShape());
        }
      }
    }

    // draw Strings
    for (StringShape ss : stringList) {
      Rectangle sb = ss.getRectangle(g2d.getFontMetrics(ss.getFont()));
      if (containShape(view, sb)) {
        g2d.setFont(ss.getFont());
        g2d.setColor(ss.getColor());
        g2d.drawString(ss.getString(), (int) sb.getX(), (int) sb.getY());

      }
    }

  }

  private boolean containShape(Rectangle view, Rectangle shapeBounds) {
    Point[] shapeCorners = new Point[4];
    shapeCorners[0] = new Point(shapeBounds.x, shapeBounds.y);
    shapeCorners[1] = new Point(shapeBounds.x, shapeBounds.y + shapeBounds.height);
    shapeCorners[2] = new Point(shapeBounds.x + shapeBounds.width, shapeBounds.y);
    shapeCorners[3] = new Point(shapeBounds.x + shapeBounds.width, shapeBounds.y + shapeBounds.height);
    for (Point point : shapeCorners) {
      if (view.contains(point)) {
        return true;
      }
    }

    Point[] viewCorners = new Point[4];
    viewCorners[0] = new Point(shapeBounds.x, shapeBounds.y);
    viewCorners[1] = new Point(shapeBounds.x, shapeBounds.y + shapeBounds.height);
    viewCorners[2] = new Point(shapeBounds.x + shapeBounds.width, shapeBounds.y);
    viewCorners[3] = new Point(shapeBounds.x + shapeBounds.width, shapeBounds.y + shapeBounds.height);
    for (Point point : viewCorners) {
      if (shapeBounds.contains(point)) {
        return true;
      }
    }

    return false;
  }

  public void addShape(ShapeWithColor shapeWithColor) {
    shapesWithColor.add(shapeWithColor);
  }

  public void addString(StringShape s) {
    stringList.add(s);
  }

  public void addExtraPainterBefore(ExtraPainter extraPainter) {
    extraPaintersBefore.add(extraPainter);
  }

}
