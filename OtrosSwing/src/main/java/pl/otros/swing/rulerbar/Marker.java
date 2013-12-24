package pl.otros.swing.rulerbar;

import java.awt.*;


public abstract class Marker {

  protected String message;
  protected Color color;
  protected float percentValue;

  public Marker(String message, Color color, float percentValue) {
    super();
    this.message = message;
    this.color = color;
    this.percentValue = percentValue;
  }


  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public float getPercentValue() {
    return percentValue;
  }

  public void setPercentValue(float percentValue) {
    this.percentValue = percentValue;
  }

  public abstract void markerClicked();

}