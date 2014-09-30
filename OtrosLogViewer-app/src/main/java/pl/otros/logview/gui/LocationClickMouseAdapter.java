package pl.otros.logview.gui;

import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;

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
    final boolean ideIntegrationEnabled = otrosApplication.getConfiguration().getBoolean(ConfKeys.JUMP_TO_CODE_ENABLED, true);
    if (!ideIntegrationEnabled) {
      return;
    }
    textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    textPane.setToolTipText("");
    try {
      JumpToCodeService jumpToCodeService = otrosApplication.getServices().getJumpToCodeService();
      if (locationInfo != null && jumpToCodeService.isJumpable(locationInfo)) {
        int lineNumber = locationInfo.getLineNumber();
        textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        StringBuilder toolTipText = new StringBuilder("<HTML>On click will open ").append(locationInfo.toString()).append(" in IDEA using JumpToCode plugin<BR/>");
        String content = jumpToCodeService.getContent(locationInfo);
        String[] split = content.split("\n");
        for (String s : split) {
          String sHtml = s.replaceAll("\t", "  ").replaceAll(" ", "&nbsp;");
          if (s.startsWith(Integer.toString(lineNumber))) {
            toolTipText.append("<B>").append(sHtml).append("</B>");
          } else {
            toolTipText.append(sHtml);
          }
          toolTipText.append("<BR/>\n");
        }
        toolTipText.append("</HTML>");
        LOGGER.fine("Tooltip text: " + toolTipText.toString());
        textPane.setToolTipText(toolTipText.toString());
      }
    } catch (IOException e1) {
      //TODO
      e1.printStackTrace();
    }
  }

  private LocationInfo getLocationInfoUnderCursor(MouseEvent e) {
    AttributeSet styleUnderCursor = getStyleUnderCursor(e);
    return (LocationInfo) styleUnderCursor.getAttribute("locationInfo");
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    LocationInfo locationInfo = getLocationInfoUnderCursor(e);
    if (locationInfo != null && otrosApplication.getConfiguration().getBoolean(ConfKeys.JUMP_TO_CODE_ENABLED, true)) {
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
