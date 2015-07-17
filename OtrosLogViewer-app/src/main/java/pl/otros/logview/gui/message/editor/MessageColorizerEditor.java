/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.message.editor;

import jsyntaxpane.DefaultSyntaxKit;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import pl.otros.logview.gui.StatusObserver;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageFragmentStyle;
import pl.otros.logview.gui.message.pattern.PropertyPatternMessageColorizer;
import pl.otros.logview.gui.util.DelayedSwingInvoke;
import pl.otros.logview.pluginable.PluginableElementListModel;
import pl.otros.logview.pluginable.PluginableElementsContainer;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageColorizerEditor extends JPanel {

  private static final String MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT = "MessageColorizerEditorDefaultContent.txt";

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageColorizerEditor.class.getName());

  private static String defualtContent;

  private JEditorPane editorPane;

  private JTextPane textPane;

  private DelayedSwingInvoke deleyedSwingInvoke;
  private PluginableElementsContainer<MessageColorizer> container;

  private StatusObserver statusObserver;

  private PluginableElementListModel<MessageColorizer> listModel;

  private String file;
  private JLabel label;

  public MessageColorizerEditor(PluginableElementsContainer<MessageColorizer> container, StatusObserver observer) {
    this.container = container;
    statusObserver = observer;
    this.setLayout(new BorderLayout());
    DefaultSyntaxKit.initKit();
    editorPane = new JEditorPane();
    JScrollPane comp = new JScrollPane(editorPane);
    JLabel propertyEditorLabel = new JLabel("Enter you message colorizer properties");
    editorPane.setContentType("text/properties");
    label = new JLabel();
    String defaultContent = getDefaultContent();
    try {
      editorPane.getDocument().insertString(0, defaultContent, null);
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
    editorPane.getDocument().addDocumentListener(documentListener);

    JLabel textPaneLabel = new JLabel("Enter log message body");
    textPane = new JTextPane();
    textPane.getDocument().addDocumentListener(documentListener);

    JPanel north = new JPanel(new BorderLayout());
    north.add(propertyEditorLabel, BorderLayout.NORTH);
    north.add(comp);
    JPanel south = new JPanel(new BorderLayout());
    south.add(textPaneLabel, BorderLayout.NORTH);
    south.add(new JScrollPane(textPane));
    JSplitPane jSplitPaneEditors = new JSplitPane(SwingConstants.HORIZONTAL, north, south);
    jSplitPaneEditors.setOneTouchExpandable(true);
    jSplitPaneEditors.setDividerLocation(0.5d);
    this.add(jSplitPaneEditors);
    this.add(label, BorderLayout.SOUTH);
  }

  protected void refreshView() {
    LOGGER.info("refreshing view");
    Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
    String text = textPane.getText();
    textPane.getStyledDocument().setCharacterAttributes(0, text.length(), defaultStyle, true);
    try {
      PropertyPatternMessageColorizer propertyPatternMessageColorize = createMessageColorizer();
      if (propertyPatternMessageColorize.colorizingNeeded(text)) {
				Collection<MessageFragmentStyle> colorize = propertyPatternMessageColorize.colorize(text);
				for (MessageFragmentStyle mfs : colorize) {
					textPane.getStyledDocument().setCharacterAttributes(mfs.getOffset(),mfs.getLength(),mfs.getStyle(),mfs.isReplace());
				}
			}
    } catch (Exception e) {
      LOGGER.error(String.format("Can't init PropertyPatternMessageColorizer:%s", e.getMessage()));
      statusObserver.updateStatus(String.format("Error: %s", e.getMessage()), StatusObserver.LEVEL_ERROR);
    }

  }

  protected PropertyPatternMessageColorizer createMessageColorizer() throws ConfigurationException {
    PropertyPatternMessageColorizer propertyPatternMessageColorizer = new PropertyPatternMessageColorizer();
    ByteArrayInputStream bin = new ByteArrayInputStream(editorPane.getText().getBytes());
    propertyPatternMessageColorizer.init(bin);
    propertyPatternMessageColorizer.setFile(file);
    return propertyPatternMessageColorizer;
  }

  protected String getDefaultContent() {
    if (defualtContent == null) {
      try {
        defualtContent = IOUtils.toString(this.getClass().getResourceAsStream(MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT));
      } catch (IOException e) {
        LOGGER.error(String.format("Can't load content of %s: %s", MESSAGE_COLORIZER_EDITOR_DEFAULT_CONTENT_TXT, e.getMessage()));
      }
    }
    return defualtContent;
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
