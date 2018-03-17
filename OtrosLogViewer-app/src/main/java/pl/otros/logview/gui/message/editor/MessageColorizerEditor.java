/*
 *******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.message.editor;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.pluginable.MessageFragmentStyle;
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.gui.message.pattern.PropertyPatternMessageColorizer;
import pl.otros.logview.gui.util.DelayedSwingInvoke;
import pl.otros.logview.gui.util.SyntaxPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

public class MessageColorizerEditor extends JPanel {

  private static final String MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT = "MessageColorizerEditorDefaultContent.txt";

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageColorizerEditor.class.getName());

  private static String defaultContent;

  private final RSyntaxTextArea editorPane;

  private final JTextPane textPane;

  private final DelayedSwingInvoke deleyedSwingInvoke;

  private final StatusObserver statusObserver;
  private Theme theme;

  private String file;
  private final JLabel label;

  MessageColorizerEditor(StatusObserver observer, Theme theme) {
    statusObserver = observer;
    this.theme = theme;
    this.setLayout(new BorderLayout());

    editorPane = SyntaxPane.propertiesTextArea(theme);
    JScrollPane comp = new JScrollPane(this.editorPane);
    JLabel propertyEditorLabel = new JLabel("Enter you message colorizer properties");
    label = new JLabel();
    String defaultContent = getDefaultContent();
    try {
      this.editorPane.getDocument().insertString(0, defaultContent, null);
    } catch (BadLocationException e1) {
      LOGGER.error(String.format("Can't set text: %s", e1.getMessage()));
    }

    deleyedSwingInvoke = new DelayedSwingInvoke() {

      @Override
      protected void performActionHook() {
        refreshView();

      }
    };
    DocumentListener documentListener = new DocumentListener() {

      @Override
      public void removeUpdate(DocumentEvent e) {
        deleyedSwingInvoke.performAction();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        deleyedSwingInvoke.performAction();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
      }
    };
    this.editorPane.getDocument().addDocumentListener(documentListener);

    JLabel textPaneLabel = new JLabel("Enter log message body");
    textPane = new JTextPane();
    textPane.getDocument().addDocumentListener(documentListener);

    JPanel north = new JPanel(new BorderLayout());
    north.add(propertyEditorLabel, BorderLayout.NORTH);
    north.add(comp);
    JPanel south = new JPanel(new BorderLayout());
    south.add(textPaneLabel, BorderLayout.NORTH);
    south.add(new JScrollPane(textPane));
    JSplitPane jSplitPaneEditors = new JSplitPane(JSplitPane.VERTICAL_SPLIT, north, south);
    jSplitPaneEditors.setOneTouchExpandable(true);
    jSplitPaneEditors.setDividerLocation(200);
    this.add(jSplitPaneEditors);
    this.add(label, BorderLayout.SOUTH);
  }

  private void refreshView() {
    LOGGER.info("refreshing view");
    Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
    String text = textPane.getText();
    textPane.getStyledDocument().setCharacterAttributes(0, text.length(), defaultStyle, true);
    try {
      PropertyPatternMessageColorizer propertyPatternMessageColorize = createMessageColorizer();
      if (propertyPatternMessageColorize.colorizingNeeded(text)) {
        Collection<MessageFragmentStyle> colorize = propertyPatternMessageColorize.colorize(text);
        for (MessageFragmentStyle mfs : colorize) {
          textPane.getStyledDocument().setCharacterAttributes(mfs.getOffset(), mfs.getLength(), mfs.getStyle(), mfs.isReplace());
        }
      }
    } catch (Exception e) {
      LOGGER.error(String.format("Can't init PropertyPatternMessageColorizer:%s", e.getMessage()));
      statusObserver.updateStatus(String.format("Error: %s", e.getMessage()), StatusObserver.LEVEL_ERROR);
    }

  }

  protected PropertyPatternMessageColorizer createMessageColorizer() throws ConfigurationException {
    PropertyPatternMessageColorizer propertyPatternMessageColorizer = new PropertyPatternMessageColorizer(theme);
    ByteArrayInputStream bin = new ByteArrayInputStream(editorPane.getText().getBytes());
    propertyPatternMessageColorizer.init(bin);
    propertyPatternMessageColorizer.setFile(file);
    return propertyPatternMessageColorizer;
  }

  private String getDefaultContent() {
    if (defaultContent == null) {
      try {
        defaultContent = IOUtils.toString(this.getClass().getResourceAsStream(MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT), "UTF-8");
      } catch (IOException e) {
        LOGGER.error(String.format("Can't load content of %s: %s", MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT, e.getMessage()));
      }
    }
    return defaultContent;
  }

  public void setTextToColorize(String text) {
    textPane.setText(text);
  }

  public String getTextToColorize() {
    return textPane.getText();
  }

  public void setMessageColorizer(PropertyPatternMessageColorizer mc) throws ConfigurationException {
    String testMessage = mc.getTestMessage();
    file = mc.getFile();
    label.setText(String.format("File: %s", mc.getFile()));
    textPane.setText(testMessage);
    mc.setTestMessage("");
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    mc.store(bout);
    editorPane.setText(new String(bout.toByteArray()));
    editorPane.setCaretPosition(0);
    mc.setTestMessage(testMessage);
    refreshView();

  }
}
