package pl.otros.logview.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.model.LocationInfo;
import pl.otros.logview.api.services.JumpToCodeService;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Optional;

class MessageDetailsMouseAdapter extends MouseAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageDetailsMouseAdapter.class.getName());
  private static final String STACKOVERFLOW_QUERY = "http://stackoverflow.com/search?q=";
  private final OtrosApplication otrosApplication;
  private final JTextPane textPane;

  MessageDetailsMouseAdapter(OtrosApplication otrosApplication, JTextPane textPane) {
    this.otrosApplication = otrosApplication;
    this.textPane = textPane;
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    Optional<LocationInfo> locationInfoOptional = getLocationInfoUnderCursor(e);
    final boolean ideIntegrationEnabled = otrosApplication.getConfiguration().getBoolean(ConfKeys.JUMP_TO_CODE_ENABLED, true);
    if (!ideIntegrationEnabled) {
      return;
    }
    textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    textPane.setToolTipText("");

    JumpToCodeService jumpToCodeService = otrosApplication.getServices().getJumpToCodeService();

    locationInfoOptional.ifPresent(
      locationInfo -> {
        try {
          if (locationInfo != null && jumpToCodeService.isJumpable(locationInfo)) {
            Optional<Integer> lineNumber = locationInfo.getLineNumber();
            textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            StringBuilder toolTipText = new StringBuilder("<HTML>On click will open ").append(locationInfo.stringForm()).append(" in IDEA using JumpToCode plugin<BR/>");
            String content = jumpToCodeService.getContent(locationInfo);
            String[] split = content.split("\n");
            for (String s : split) {
              String sHtml = s.replaceAll("\t", "  ").replaceAll(" ", "&nbsp;");
              if (s.startsWith(Integer.toString(lineNumber.get()))) {
                toolTipText.append("<B>").append(sHtml).append("</B>");
              } else {
                toolTipText.append(sHtml);
              }
              toolTipText.append("<BR/>\n");
            }
            toolTipText.append("</HTML>");
            LOGGER.debug("Tooltip text: " + toolTipText.toString());
            textPane.setToolTipText(toolTipText.toString());
          }
        } catch (IOException e1) {
          LOGGER.warn("Can't get location info details for tooltip.",e1);
        }
      }
    );

    final Optional<String> exceptionNameAndMsgUnderCursor = getExceptionNameAndMsgUnderCursor(e);
    exceptionNameAndMsgUnderCursor.ifPresent(msg -> {
      textPane.setToolTipText("Open in browser: " + STACKOVERFLOW_QUERY + msg);
      textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    });
  }

  private Optional<LocationInfo> getLocationInfoUnderCursor(MouseEvent e) {
    AttributeSet styleUnderCursor = getStyleUnderCursor(e);
    return Optional.ofNullable((LocationInfo) styleUnderCursor.getAttribute("locationInfo"));
  }

  private Optional<String> getExceptionNameAndMsgUnderCursor(MouseEvent e) {
    AttributeSet styleUnderCursor = getStyleUnderCursor(e);
    final String exceptionMessage = (String) styleUnderCursor.getAttribute("exceptionMessage");
    return Optional.ofNullable(exceptionMessage);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Optional<LocationInfo> locationInfoOptional = getLocationInfoUnderCursor(e);
    locationInfoOptional.ifPresent(locationInfo -> {
        if (otrosApplication.getConfiguration().getBoolean(ConfKeys.JUMP_TO_CODE_ENABLED, true)) {
          JumpToCodeService jumpToCodeService = otrosApplication.getServices().getJumpToCodeService();
          LOGGER.debug("Is jump available: " + jumpToCodeService.isIdeAvailable());
          try {
            if (jumpToCodeService.isIdeAvailable() && jumpToCodeService.isJumpable(locationInfo)) {
              LOGGER.info("Jumping to " + locationInfo);
              jumpToCodeService.jump(locationInfo);
            }
          } catch (IOException e1) {
            otrosApplication.getStatusObserver().updateStatus("Can't go to location " + locationInfo.toString(), StatusObserver.LEVEL_WARNING);
            LOGGER.warn("Can't open location in IDE", e1);
          }
        }
      }
    );

    getExceptionNameAndMsgUnderCursor(e).ifPresent(msg -> {
      try {
        final String uri = STACKOVERFLOW_QUERY + URLEncoder.encode(msg, "UTF-8");
        Desktop.getDesktop().browse(new URI(uri));
      } catch (IOException | URISyntaxException e1) {
        LOGGER.warn("Can't open browser with URI with query: " + msg, e1);
      }
    });
  }

  private AttributeSet getStyleUnderCursor(MouseEvent e) {
    int i = textPane.viewToModel(e.getPoint());
    return textPane.getStyledDocument().getCharacterElement(i).getAttributes();
  }
}
