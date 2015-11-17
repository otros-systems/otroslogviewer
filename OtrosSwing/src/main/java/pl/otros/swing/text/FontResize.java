package pl.otros.swing.text;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FontResize {

  public static final String DECREASE_FONT_SIZE = "Decrease font size";
  public static final String INCREASE_FONT_SIZE = "Increase font size";

  public static void addFontResizeOnScroll(Component scrollPane) {
    scrollPane.addMouseWheelListener(event -> {
      if (event.isMetaDown()) {
        final int unitsToScroll = -event.getUnitsToScroll();
        changeFontSize(getDeepestComponent(scrollPane, event.getPoint()), unitsToScroll);
      }
    });
  }

  public static Component getDeepestComponent(Component component, Point point) {
    final Component componentAt = component.getComponentAt(point);
    if (componentAt == null || component == componentAt) {
      return component;
    } else {
      return getDeepestComponent(componentAt, point);
    }
  }

  private static void changeFontSize(Component componentAt, int delta) {
    if (componentAt instanceof JTextArea || componentAt instanceof JEditorPane) {
      final Font font = componentAt.getFont();
      final int newSize = Math.max(font.getSize() + delta, 8);
      componentAt.setFont(font.deriveFont((float) newSize));
    }
  }

  /**
   * Create action bounded with JTextArea to increase font size
   * @param textArea bounded with action
   * @return action
   */
  public static Action increaseFontSizeAction(JTextArea textArea) {
    return changeFontSizeAction(textArea, "A+", INCREASE_FONT_SIZE, 1);
  }


  /**
   * Create action bounded with JTextPane to increase font size
   * @param textPane bounded with action
   * @return action
   */
  public static Action increaseFontSizeAction(JTextPane textPane) {
    return changeFontSizeAction(textPane, "A+", INCREASE_FONT_SIZE, 1);
  }



  /**
   * Create action bounded with JTextArea to decrease font size
   * @param textArea bounded with action
   * @return action
   */
  public static Action decreaseFontSizeAction(JTextArea textArea) {
    return changeFontSizeAction(textArea, "A-", DECREASE_FONT_SIZE, -1);
  }


  /**
   * Create action bounded with JTextPane to decrease font size
   * @param textPane bounded with action
   * @return action
   */
  public static Action decreaseFontSizeAction(JTextPane textPane) {
    return changeFontSizeAction(textPane, "A-", DECREASE_FONT_SIZE, -1);
  }

  private static Action changeFontSizeAction(JTextComponent textComponent, String name, String tooltip, int size) {
    final AbstractAction abstractAction = new AbstractAction(name) {

      @Override
      public void actionPerformed(ActionEvent e) {
        changeFontSize(textComponent, size);
      }
    };

    abstractAction.putValue(Action.SHORT_DESCRIPTION, tooltip);
    return abstractAction;
  }
}
