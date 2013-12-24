package pl.otros.swing.rulerbar;

import javax.swing.text.JTextComponent;
import java.awt.*;

public class LineInTextMarker extends Marker {

  private int caretPosition;
  private JTextComponent textComponent;

  public LineInTextMarker(String message, Color color, float percentValue,
                          int caretPosition, JTextComponent jTextPane) {
    super(message, color, percentValue);
    this.caretPosition = caretPosition;
    textComponent = jTextPane;
  }

  @Override
  public void markerClicked() {
    textComponent.setCaretPosition(caretPosition);
  }

  @Override
  public String toString() {
    return "LineInTextMarker [caretPosition=" + caretPosition
        + ", message=" + message + "]";
  }


}
