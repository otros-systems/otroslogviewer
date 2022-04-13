package pl.otros.swing.rulerbar;

import javax.swing.text.JTextComponent;
import java.util.List;
import java.awt.*;

public class RulerBarHelper {

  public enum TooltipMode {
    NO_PREFIX, LINE_NUMBER_PREFIX
  }

  public static <T extends JTextComponent> OtrosJTextWithRulerScrollPane<T> wrapTextComponent(
      T textComponent) {
    OtrosJTextWithRulerScrollPane<T> otrosJTextWithRulerScrollPane = new OtrosJTextWithRulerScrollPane<>(
      textComponent);
    otrosJTextWithRulerScrollPane.getRulerBar().addMarkerClickListener(
        new ScrollToSelectedMarker());
    return otrosJTextWithRulerScrollPane;
  }

  /**
   * Add Marker at select position
   * @param pane OtrosJTextWithRulerScrollPane
   * @param charPosition position
   * @param tooltip Tooltip message
   * @param color marker color
   * @param tooltipMode Tooltip mode
   */
  public static void addTextMarkerToPosition(
      OtrosJTextWithRulerScrollPane<? extends JTextComponent> pane,
      int charPosition, final String tooltip, Color color,
      TooltipMode tooltipMode) {

    String tooltipText = tooltip;
    MarkerModel markerModel = pane.getRulerBar().getMarkerModel();
    String text = pane.getjTextComponent().getText();
    int lines = countLines(text);
    int lineOfCharPosition = countLineOfCharPosition(charPosition, text);
    float percentValue = (float) lineOfCharPosition
        / Math.max(lines - 1, 1);
    if (TooltipMode.LINE_NUMBER_PREFIX.equals(tooltipMode)) {
      tooltipText = "Line " + lineOfCharPosition + ": " + tooltipText;
    }
    LineInTextMarker marker = new LineInTextMarker(tooltipText, color,
        percentValue, charPosition, pane.getjTextComponent());
    markerModel.addMarker(marker);
  }

  /**
   * Remove all markers
   * @param wrapTextComponent OtrosJTextWithRulerScrollPane
   */
  public static void clearMarkers(
      OtrosJTextWithRulerScrollPane<? extends JTextComponent> wrapTextComponent) {
    wrapTextComponent.getMarkerModel().clear();
  }

  private static int countLines(String text) {
    int lines = 1;
    int indexOf = 0;
    while ((indexOf = text.indexOf('\n', indexOf + 1)) > -1) {
      lines++;
    }
    return lines;
  }

  private static int countLineOfCharPosition(int charPosition, String text) {
    int line = 0;
    int indexOf = 0;
    while ((indexOf = text.indexOf('\n', indexOf + 1)) > -1) {
      if (indexOf > charPosition) {
        break;
      }
      line++;
    }
    return line;
  }

  /**
   * Scroll to first marker if found
   * @param pane OtrosJTextWithRulerScrollPane
   */
  public static void scrollToFirstMarker(OtrosJTextWithRulerScrollPane pane) {
    List<Marker> markers = pane.getMarkerModel().getMarkers();
    if (markers.size()>0){
      Marker marker = markers.get(0);
      marker.markerClicked();
    }
  }

}
