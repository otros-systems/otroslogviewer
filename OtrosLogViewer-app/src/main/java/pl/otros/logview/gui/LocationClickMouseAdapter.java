package pl.otros.logview.gui;

import pl.otros.logview.gui.message.LocationInfo;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class LocationClickMouseAdapter extends MouseAdapter {
  private final OtrosApplication otrosApplication;
  private JTextPane textPane;

  public LocationClickMouseAdapter(OtrosApplication otrosApplication, JTextPane textPane) {
    this.otrosApplication = otrosApplication;
    this.textPane = textPane;
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    LocationInfo locationInfo = getLocationInfoUnsweCursor(e);
    if (locationInfo != null) {
      textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      textPane.setToolTipText("On click will open " + locationInfo.toString() + " in IDEA using JumpToCode plugin");
    } else {
      textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      textPane.setToolTipText("");
    }
  }

  private LocationInfo getLocationInfoUnsweCursor(MouseEvent e) {
    AttributeSet styleUnderCursor = getStyleUnderCursor(e);
    return (LocationInfo) styleUnderCursor.getAttribute("locationInfo");
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    LocationInfo locationInfo = getLocationInfoUnsweCursor(e);
    if (locationInfo != null) {
      JOptionPane.showMessageDialog(otrosApplication.getApplicationJFrame(), "Clicked at \"" + locationInfo.toString() + "\"");
    }
  }

  AttributeSet getStyleUnderCursor(MouseEvent e) {
    int i = textPane.viewToModel(e.getPoint());
    return textPane.getStyledDocument().getCharacterElement(i).getAttributes();
  }
}
