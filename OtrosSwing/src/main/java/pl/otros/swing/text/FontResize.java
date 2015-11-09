package pl.otros.swing.text;

import javax.swing.*;
import java.awt.*;

public class FontResize {

  public static void addFontResizeOnScroll(Component scrollPane) {
    scrollPane.addMouseWheelListener(event -> {
      if (event.isMetaDown()) {
        final int unitsToScroll = - event.getUnitsToScroll();
        changeFontSize(getDeepestComponent(scrollPane,event.getPoint()), unitsToScroll);
      }
    });
  }

  public static Component getDeepestComponent(Component component, Point point){
    final Component componentAt = component.getComponentAt(point);
    if (componentAt == null || component == componentAt){
      return component;
    } else {
      return getDeepestComponent(componentAt,point);
    }
  }

  private static void changeFontSize(Component componentAt, int unitsToScroll) {
    if (componentAt instanceof JTextArea || componentAt instanceof JEditorPane) {
      final Font font = componentAt.getFont();
      final int newSize = Math.max(font.getSize() + unitsToScroll, 8);
      componentAt.setFont(font.deriveFont((float)newSize));
    }
  }
}
