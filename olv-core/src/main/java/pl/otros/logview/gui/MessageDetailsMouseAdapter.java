package pl.otros.logview.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;

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
    textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    textPane.setToolTipText("");

    final Optional<String> exceptionNameAndMsgUnderCursor = getExceptionNameAndMsgUnderCursor(e);
    exceptionNameAndMsgUnderCursor.ifPresent(msg -> {
      textPane.setToolTipText("Open in browser: " + STACKOVERFLOW_QUERY + msg);
      textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    });
  }

  private Optional<String> getExceptionNameAndMsgUnderCursor(MouseEvent e) {
    AttributeSet styleUnderCursor = getStyleUnderCursor(e);
    final String exceptionMessage = (String) styleUnderCursor.getAttribute("exceptionMessage");
    return Optional.ofNullable(exceptionMessage);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
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
