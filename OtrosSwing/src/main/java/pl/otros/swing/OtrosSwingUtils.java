package pl.otros.swing;

import javax.swing.*;

public class OtrosSwingUtils {

  public static void frameToFront(JFrame frame){
    int state = frame.getExtendedState();
    state &= ~JFrame.ICONIFIED;
    frame.setExtendedState(state);
    frame.setAlwaysOnTop(true);
    frame.toFront();
    frame.requestFocus();
    frame.setAlwaysOnTop(false);
  }
}
