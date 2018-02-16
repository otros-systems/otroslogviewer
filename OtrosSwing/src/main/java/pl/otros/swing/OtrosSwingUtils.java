package pl.otros.swing;

import javax.swing.*;

public class OtrosSwingUtils {

  public static void frameToFront(JFrame frame) {
    int state = frame.getExtendedState();
    state &= ~JFrame.ICONIFIED;
    frame.setExtendedState(state);
    frame.setAlwaysOnTop(true);
    frame.toFront();
    frame.requestFocus();
    frame.setAlwaysOnTop(false);
  }

  public static JButton fontSize2(JButton button) {
    button.setFont(button.getFont().deriveFont(new JButton().getFont().getSize() * 2f));
    return button;
  }
  public static JLabel fontSize2(JLabel label) {
    label.setFont(label.getFont().deriveFont(new JLabel().getFont().getSize() * 2f));
    return label;
  }
}
