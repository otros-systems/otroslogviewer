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

import java.awt.*;
import java.io.InputStream;

public class UMLModelData {

  protected int distanceBetweenActors = 100;
  protected Color actorColor = Color.BLACK;
  protected Color boxColor = Color.BLACK;
  protected Color messageColor = new Color(178, 34, 34); // Brown
  protected Color messageStringColor = Color.BLUE;
  protected Color backgroundColor = Color.WHITE;
  protected Color lifeLinesColor = Color.GRAY;
  protected double boxWidth = 10;
  protected double actorSizeX = 80;
  protected double actorSizeY = 40;
  protected double messageArrowSize = 5;
  protected double selfMessageWidth = 20;
  protected Font actorFont = new Font("Arial", Font.BOLD, 10);
  protected int yStep = 17;
  protected Font messageFont = new Font("Arial", Font.PLAIN, 11);

  public void loadProperties(InputStream in) {

  }

  public int getDistanceBetweenActors() {
    return distanceBetweenActors;
  }

  public void setDistanceBetweenActors(int distanceBetweenActors) {
    this.distanceBetweenActors = distanceBetweenActors;
  }

  public Color getActorColor() {
    return actorColor;
  }

  public void setActorColor(Color actorColor) {
    this.actorColor = actorColor;
  }

  public Color getBoxColor() {
    return boxColor;
  }

  public void setBoxColor(Color boxColor) {
    this.boxColor = boxColor;
  }

  public Color getMessageColor() {
    return messageColor;
  }

  public void setMessageColor(Color messageColor) {
    this.messageColor = messageColor;
  }

  public Color getMessageStringColor() {
    return messageStringColor;
  }

  public void setMessageStringColor(Color messageStringColor) {
    this.messageStringColor = messageStringColor;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public double getBoxWidth() {
    return boxWidth;
  }

  public void setBoxWidth(double boxWidth) {
    this.boxWidth = boxWidth;
  }

  public double getActorSizeX() {
    return actorSizeX;
  }

  public void setActorSizeX(double actorSizeX) {
    this.actorSizeX = actorSizeX;
  }

  public double getActorSizeY() {
    return actorSizeY;
  }

  public void setActorSizeY(double actorSizeY) {
    this.actorSizeY = actorSizeY;
  }

  public double getMessageArrowSize() {
    return messageArrowSize;
  }

  public void setMessageArrowSize(double messageArrowSize) {
    this.messageArrowSize = messageArrowSize;
  }

  public double getSelfMessageWidth() {
    return selfMessageWidth;
  }

  public void setSelfMessageWidth(double selfMessageWidth) {
    this.selfMessageWidth = selfMessageWidth;
  }

  public Font getActorFont() {
    return actorFont;
  }

  public void setActorFont(Font actorFont) {
    this.actorFont = actorFont;
  }

  public int getYStep() {
    return yStep;
  }

  public void setYStep(int step) {
    yStep = step;
  }

  public Font getMessageFont() {
    return messageFont;
  }

  public void setMessageFont(Font messageFont) {
    this.messageFont = messageFont;
  }

}
