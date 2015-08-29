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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class UMLModel {

  private int yPosition;
  private final LinkedList<String> actors;
  private final SelectableJComponent cContent;
  private final SelectableJComponent cHeader;
  private final JScrollPane spContent;
  private final JPanel panel;
  private final HashMap<String, Integer> actorPosition;
  private final HashMap<String, Integer> actorActivation;
  private final LogUmlMapper logUmlMapper;
  private final UMLModelData data;

  public UMLModel(LinkedList<String> actors) {
    this.actors = actors;
    data = new UMLModelData();

    cContent = new SelectableJComponent();
    cContent.setBackground(data.backgroudColor);

    cHeader = new SelectableJComponent();
    cHeader.setBackground(data.backgroudColor);
    actorPosition = new LinkedHashMap<>();
    actorActivation = new HashMap<>();
    logUmlMapper = new LogUmlMapper();

    int[] xPositions = new int[actors.size()];
    for (int i = 0; i < actors.size(); i++) {
      int xPosition = data.distanceBetweenActors / 2 + data.distanceBetweenActors * i;
      actorPosition.put(actors.get(i), xPosition);
      xPositions[i] = xPosition;
    }

    yPosition = 5;
    for (String actor : actors) {
      Rectangle2D.Double r = new Rectangle2D.Double(actorPosition.get(actor) - data.actorSizeX / 2, yPosition, data.actorSizeX, data.actorSizeY);
      cHeader.addShape(new ShapeWithColor(data.actorColor, r));
      Rectangle2D.Double r2 = new Rectangle2D.Double(actorPosition.get(actor) - data.actorSizeX / 2 + 1, yPosition + 1, data.actorSizeX - 1,
          data.actorSizeY - 1);
      cHeader.addShape(new ShapeWithColor(data.backgroudColor, r2, true));
      cHeader.addString(new StringShape(convertClassName(actor), data.actorFont, actorPosition.get(actor), (int) (yPosition + data.actorSizeY / 2),
          data.actorColor));

    }

    cHeader.addExtraPainterBefore(new LifelinePainter(xPositions, data.lifeLinesColor));
    cContent.addExtraPainterBefore(new LifelinePainter(xPositions, data.lifeLinesColor));

    Dimension dimensionHeader = new Dimension((actors.size()) * data.distanceBetweenActors + 30 + 320, (int) (yPosition + data.actorSizeY + 10));
    cHeader.setSize(dimensionHeader);
    cHeader.setMinimumSize(dimensionHeader);
    cHeader.setPreferredSize(dimensionHeader);
    yPosition = 0;

    JScrollPane spHeader = new JScrollPane(cHeader);
    spHeader.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    spHeader.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    spContent = new JScrollPane(cContent);
    spHeader.getHorizontalScrollBar().setModel(spContent.getHorizontalScrollBar().getModel());
    spContent.getVerticalScrollBar().setUnitIncrement(50);
    spContent.getHorizontalScrollBar().setUnitIncrement(10);

    panel = new JPanel(new BorderLayout());
    panel.add(spHeader, BorderLayout.NORTH);
    panel.add(spContent, BorderLayout.CENTER);
  }

  public void activate(String actor, int logId) {
    actorActivation.put(actor, yPosition);
    logUmlMapper.addMapping(logId, new Point((int) (actorPosition.get(actor) - data.actorSizeX), yPosition));
  }

  public void deactivate(String actor, int logId) {
    logUmlMapper.addMapping(logId, new Point((int) (actorPosition.get(actor) - data.actorSizeX), yPosition));

    if (actorActivation.containsKey(actor)) {
      int yPos = actorActivation.remove(actor);
      double x = actorPosition.get(actor) - data.boxWidth / 2;
      ShapeWithColor boxOutline = new ShapeWithColor(data.boxColor, new Rectangle2D.Double(x, yPos, data.boxWidth, yPosition - yPos));
      ShapeWithColor boxInside = new ShapeWithColor(data.backgroudColor, new Rectangle2D.Double(x, yPos, data.boxWidth, yPosition - yPos), true);
      cContent.addShape(boxInside);
      cContent.addShape(boxOutline);
    }
  }

  public void message(String source, String destination, String message, int logId) {
    logUmlMapper.addMapping(logId, new Point((int) (actorPosition.get(source) - data.actorSizeX), yPosition));

    if (source.equalsIgnoreCase(destination)) {
      selfMessage(source, message, logId);
      return;
    }
    int xS = actorPosition.get(source);
    int xD = actorPosition.get(destination);
    for (Shape shape : getArrow(xS, xD, yPosition)) {
      cContent.addShape(new ShapeWithColor(data.messageColor, shape));
    }
    xS = (xS < xD) ? xS : xD;
    cContent.addString(new StringShape(message, data.messageFont, xS + data.distanceBetweenActors / 2, yPosition, data.messageStringColor));
  }

  public void rmessage(String source, String destination, String message, int logId) {
    logUmlMapper.addMapping(logId, new Point((int) (actorPosition.get(source) - data.actorSizeX), yPosition));
    int xS = actorPosition.get(source);
    int xD = actorPosition.get(destination);
    for (Shape shape : getArrow(xD, xS, yPosition)) {
      cContent.addShape(new ShapeWithColor(data.messageColor, shape));
    }

    xS = (xS < xD) ? xS : xD;
    cContent.addString(new StringShape(message, data.messageFont, xS + data.distanceBetweenActors / 2, yPosition, data.messageStringColor));
  }

  // TODO ciagnie strzalke az do momentu wyjscia z metody
  private void selfMessage(String source, String message, int logId) {
    int x = actorPosition.get(source);
    x = x + (int) (data.boxWidth / 2);
    cContent.addShape(new ShapeWithColor(data.messageColor, new Line2D.Double(x, yPosition, x + data.selfMessageWidth, yPosition)));
    cContent.addShape(new ShapeWithColor(data.messageColor, new Line2D.Double(x + data.selfMessageWidth, yPosition, x + data.selfMessageWidth, yPosition
        + data.yStep)));
    cContent.addShape(new ShapeWithColor(data.messageColor, new Line2D.Double(x, yPosition + data.yStep, x + data.selfMessageWidth, yPosition + data.yStep)));

    int xArrowHead2 = (int) (x + data.messageArrowSize);
    cContent.addShape(new ShapeWithColor(data.messageColor, new Line2D.Double(x, yPosition + data.yStep, xArrowHead2, yPosition + data.yStep
        - data.messageArrowSize)));
    cContent.addShape(new ShapeWithColor(data.messageColor, new Line2D.Double(x, yPosition + data.yStep, xArrowHead2, yPosition + data.yStep
        + data.messageArrowSize)));
    cContent.addString(new StringShape(message, data.messageFont, x + data.distanceBetweenActors / 2, yPosition + data.yStep / 2,
        data.messageStringColor));
    step();
  }

  /**
   * Gets horizontal arrow on y height from x=s to x=d
   * 
   * @param s
   * @param d
   * @param y
   * @return
   */
  private Shape[] getArrow(int s, int d, int y) {
    s += Math.signum(d - s) * data.boxWidth / 2;
    d -= Math.signum(d - s) * data.boxWidth / 2;
    Shape[] shapes = new Shape[2];
    shapes[0] = new Line2D.Double(s, y, d, y);
    Polygon polygon = new Polygon();
    polygon.addPoint(d, y);
    polygon.addPoint((int) (d - Math.signum(d - s) * data.messageArrowSize), (int) (y - data.messageArrowSize));
    polygon.addPoint((int) (d - Math.signum(d - s) * data.messageArrowSize), (int) (y + data.messageArrowSize));
    shapes[1] = polygon;
    return shapes;
  }

  private String convertClassName(String clazz) {
    StringBuilder sb = new StringBuilder();
    if (clazz.indexOf('.') < 0) {
      sb.append(clazz);
    } else {
      int lastDot = clazz.lastIndexOf('.');
      // int currentDot = -1;
      // while (currentDot < lastDot ){
      // sb.append(clazz.charAt(currentDot+1));
      // sb.append('.');
      // currentDot=clazz.indexOf('.', currentDot+1);
      // }
      // sb.append(clazz.substring(lastDot));
      sb.append(clazz.substring(lastDot + 1));
    }
    if (sb.length() > 30) {
      sb.setLength(30);
    }

    return sb.toString();
  }

  public void step() {
    yPosition += data.yStep;
  }

  public void step(int step) {
    yPosition += step;

  }

  public JComponent getJComponent() {
    Dimension d = new Dimension((actors.size()) * data.distanceBetweenActors + 30 + 320, yPosition + 20);
    cContent.setSize(d);
    cContent.setPreferredSize(d);
    cContent.setMinimumSize(d);
    return panel;
  }

  public JComponent getContentJComponet() {
    return cContent;
  }

  public JScrollPane getScrollPane() {
    return spContent;
  }

  public LogUmlMapper getLogUmlMapper() {
    return logUmlMapper;
  }

}
