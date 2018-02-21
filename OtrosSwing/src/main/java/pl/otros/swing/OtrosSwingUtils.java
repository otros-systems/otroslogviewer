package pl.otros.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class OtrosSwingUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(OtrosSwingUtils.class.getName());

  public static void frameToFront(JFrame frame) {
    int state = frame.getExtendedState();
    state &= ~JFrame.ICONIFIED;
    frame.setExtendedState(state);
    frame.setAlwaysOnTop(true);
    frame.toFront();
    frame.requestFocus();
    frame.setAlwaysOnTop(false);
  }

  public static <T extends JComponent> T fontSize2(T component) {
    try {
      final int newSize = component.getClass().newInstance().getFont().getSize() * 2;
      final Font oldFont = component.getFont();
      final Font newFont = new Font(oldFont.getFontName(), oldFont.getStyle(), newSize);
      component.setFont(newFont);
    } catch (InstantiationException | IllegalAccessException e) {
      LOGGER.warn("Can't create new instance of " + component.getClass() + " to get default font size");
    }

    return component;
  }
}
