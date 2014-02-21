package pl.otros.logview.gui;

import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.gui.services.jumptocdoe.JumpToCodeService;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class LocationClickMouseAdapter extends MouseAdapter {
  private static final Logger LOGGER = Logger.getLogger(LocationClickMouseAdapter.class.getName());
  private final OtrosApplication otrosApplication;
  private JTextPane textPane;

  public LocationClickMouseAdapter(OtrosApplication otrosApplication, JTextPane textPane) {
    this.otrosApplication = otrosApplication;
    this.textPane = textPane;
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    LocationInfo locationInfo = getLocationInfoUnderCursor(e);

    textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    textPane.setToolTipText("");
    try {
      JumpToCodeService jumpToCodeService = otrosApplication.getServices().getJumpToCodeService();
      if (locationInfo != null && jumpToCodeService.isJumpable(locationInfo)) {

        textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        String toolTipText = "<HTML>On click will open " + locationInfo.toString() + " in IDEA using JumpToCode plugin<BR/>";
        toolTipText+=jumpToCodeService.getContent(locationInfo).replace("\n","<BR/>").replaceAll("\t","  ").replaceAll(" ","&nbsp;")+"</HTML>";
        LOGGER.info("Tooltip text: " + toolTipText);
        textPane.setToolTipText(toolTipText);
      }
    } catch (IOException e1) {
      //TODO
      e1.printStackTrace();
    }
  }

  private LocationInfo getLocationInfoUnderCursor(MouseEvent e) {
    AttributeSet styleUnderCursor = getStyleUnderCursor(e);
    LocationInfo locationInfo = (LocationInfo) styleUnderCursor.getAttribute("locationInfo");
    return locationInfo;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    LocationInfo locationInfo = getLocationInfoUnderCursor(e);
    if (locationInfo != null) {
      JumpToCodeService jumpToCodeService = otrosApplication.getServices().getJumpToCodeService();
      LOGGER.fine("Is jump available: " + jumpToCodeService.isIdeAvailable());
      try {
        if (jumpToCodeService.isIdeAvailable() && jumpToCodeService.isJumpable(locationInfo)) {
          LOGGER.info("Jumping to " + locationInfo);
          jumpToCodeService.jump(locationInfo);
        }
      } catch (IOException e1) {
        otrosApplication.getStatusObserver().updateStatus("Can't go to location " + locationInfo.toString(), StatusObserver.LEVEL_WARNING);
        LOGGER.log(Level.WARNING, "Can't open location in IDE", e1);
      }
    }
  }

  AttributeSet getStyleUnderCursor(MouseEvent e) {
    int i = textPane.viewToModel(e.getPoint());
    return textPane.getStyledDocument().getCharacterElement(i).getAttributes();
  }
}
