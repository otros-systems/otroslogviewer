/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.gui.message.update;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.MessageFormatter;
import pl.otros.logview.api.pluginable.MessageFragmentStyle;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.logview.gui.LogViewMainFrame;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.gui.renderers.LevelRenderer;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
public class LogDataFormatter {

  public static final String NEW_LINE = "\n";
  private static final Logger LOGGER = LoggerFactory.getLogger(LogDataFormatter.class.getName());

  private final LogData ld;
  private final ArrayList<TextChunkWithStyle> chunks = new ArrayList<>();
  private final DateFormat dateFormat;
  private Style mainStyle = null;
  private Style classMethodStyle = null;
  private Style boldArialStyle = null;
  private Style propertyNameStyle = null;
  private Style propertyValueStyle = null;

  private final StyleContext sc;


  private int maximumMessageSize;

  private final PluginableElementsContainer<MessageColorizer> colorizersContainer;
  private final PluginableElementsContainer<MessageFormatter> formattersContainer;
  private final CancelStatus cancelStatus;

  private final MessageUpdateUtils messageUtils;
  private OtrosApplication otrosApplication;

  public LogDataFormatter(OtrosApplication otrosApplication,
                          LogData logData, //
                          DateFormat dateFormat,//
                          MessageUpdateUtils messageUtils,//
                          PluginableElementsContainer<MessageColorizer> colorizersContainer,//
                          PluginableElementsContainer<MessageFormatter> formattersContainer,//
                          CancelStatus cancelStatus, int maximumMessageSize) {
    this.otrosApplication = otrosApplication;
    this.ld = logData;
    this.dateFormat = dateFormat;
    this.messageUtils = messageUtils;
    this.colorizersContainer = colorizersContainer;
    this.formattersContainer = formattersContainer;
    this.cancelStatus = cancelStatus;
    this.maximumMessageSize = maximumMessageSize;


    sc = new StyleContext();
    Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
    mainStyle = sc.addStyle("MainStyle", defaultStyle);
    StyleConstants.setForeground(mainStyle, Color.BLACK);

    classMethodStyle = sc.addStyle("classMethod", null);
    StyleConstants.setFontFamily(classMethodStyle, "monospaced");
    StyleConstants.setForeground(classMethodStyle, Color.BLUE);
    boldArialStyle = sc.addStyle("note", mainStyle);
    StyleConstants.setFontFamily(boldArialStyle, "arial");
    StyleConstants.setBold(boldArialStyle, true);

    propertyNameStyle = sc.addStyle("propertyValue", classMethodStyle);
    StyleConstants.setForeground(propertyNameStyle, new Color(0, 0, 128));

    propertyValueStyle = sc.addStyle("propertyValue", classMethodStyle);
    StyleConstants.setForeground(propertyNameStyle, new Color(0, 128, 0));


  }

  private TextChunkWithStyle getDateChunk() {
    String s1 = "Date:    " + dateFormat.format(ld.getDate()) + NEW_LINE;
    return new TextChunkWithStyle(s1, mainStyle);
  }

  private TextChunkWithStyle getClassChunk() {
    String s1 = "Class:   " + ld.getClazz() + NEW_LINE;
    return new TextChunkWithStyle(s1, classMethodStyle);
  }

  private TextChunkWithStyle getMethodChunk() {
    String s1 = "Method:  " + ld.getMethod() + NEW_LINE;
    return new TextChunkWithStyle(s1, classMethodStyle);
  }

  private void addLevelChunk() {
    String s1 = "Level:   ";
    chunks.add(new TextChunkWithStyle(s1, classMethodStyle));
    Icon levelIcon = LevelRenderer.getIconByLevel(ld.getLevel());
    if (levelIcon != null) {
      chunks.add(new TextChunkWithStyle(levelIcon));
    }
    s1 = " " + ld.getLevel().getName() + NEW_LINE;
    chunks.add(new TextChunkWithStyle(s1, classMethodStyle));
  }

  private void addFileChunk() {
    String s1 = null;
    if (StringUtils.isNotBlank(ld.getFile())) {
      s1 = "File: " + ld.getFile();
      if (StringUtils.isNotBlank(ld.getLine())) {
        s1 = s1 + ":" + ld.getLine();
      }
      chunks.add(new TextChunkWithStyle(s1 + NEW_LINE, mainStyle));
    }
  }

  private void addNDCChunk() {
    if (StringUtils.isNotBlank(ld.getNDC())) {
      chunks.add(new TextChunkWithStyle("NDC: " + ld.getNDC() + NEW_LINE, mainStyle));
    }
  }

  private void addLoggerNameChunk() {
    if (StringUtils.isNotBlank(ld.getLoggerName())) {
      chunks.add(new TextChunkWithStyle("Logger name: " + ld.getLoggerName() + NEW_LINE, mainStyle));
    }
  }

  private void addPropertiesChunk() {
    Map<String, String> properties = ld.getProperties();
    if (properties != null && properties.size() > 0) {
      chunks.add(new TextChunkWithStyle("Properties:\n", boldArialStyle));
      ArrayList<String> keys = new ArrayList(properties.keySet());
      Collections.sort(keys);
      for (String key : keys) {
        chunks.add(new TextChunkWithStyle(key + "=", propertyNameStyle));
        chunks.add(new TextChunkWithStyle(properties.get(key) + "\n", propertyValueStyle));
      }
    }
  }

  private void addMessageChunk() {
    String s1 = "Message: ";
    chunks.add(new TextChunkWithStyle(s1, boldArialStyle));
    s1 = ld.getMessage();
    if (s1.length() > maximumMessageSize) {
      int removedCharsSize = s1.length() - maximumMessageSize;
      s1 = StringUtils.left(s1, maximumMessageSize) + String.format("%n...%n...(+%,d chars)", removedCharsSize);
    }

    Collection<MessageFormatter> formatters = formattersContainer.getElements();
    int charsBeforeMessage = countCharsBeforeMessage(chunks);
    for (MessageFormatter messageFormatter : formatters) {
      if (cancelStatus.isCancelled()) {
        return;
      }
      s1 = messageUtils.formatMessageWithTimeLimit(s1, messageFormatter, 5);
      s1 = StringUtils.remove(s1, '\r');
    }
    chunks.add(new TextChunkWithStyle(s1, mainStyle));


    Collection<MessageColorizer> colorizers = colorizersContainer.getElements();
    ArrayList<MessageFragmentStyle> messageFragmentStyles = new ArrayList<>();
    for (MessageColorizer messageColorizer : colorizers) {
      if (messageColorizer.getPluginableId().equals(SearchResultColorizer.class.getName())) {
        continue;
      }
      messageFragmentStyles.addAll(messageUtils.colorizeMessageWithTimeLimit(s1, charsBeforeMessage, messageColorizer, 5));
    }

    chunks.addAll(messageFragmentStyles.stream().map(messageFragmentStyle -> new TextChunkWithStyle(null, messageFragmentStyle)).collect(Collectors.toList()));

  }

  private void addMarkedChunk() {
    chunks.add(new TextChunkWithStyle("\nMarked: ", boldArialStyle));
    if (ld.isMarked()) {
      MarkerColors markerColors = ld.getMarkerColors();
      chunks.add(new TextChunkWithStyle(" " + markerColors.name() + NEW_LINE, getStyleForMarkerColor(markerColors)));
    } else {
      chunks.add(new TextChunkWithStyle("false\n", boldArialStyle));
    }
  }

  private void addNoteChunk() {
    String s1 = null;
    Note note = ld.getNote();
    if (note != null && note.getNote() != null && note.getNote().length() > 0) {
      s1 = "Note: " + note.getNote();
      chunks.add(new TextChunkWithStyle(s1, boldArialStyle));
    }

  }

  public java.util.List<TextChunkWithStyle> format() throws Exception {

    LOGGER.trace("Start do in background");
    if (otrosApplication.getConfiguration() != null) {
      //try to get chunks order and set default if not in config
      String chunksOrder = null;
      chunksOrder = otrosApplication.getConfiguration().getString(ConfKeys.MESSAGE_FORMATTER_CHUNKS_ORDER,
        "date;class;method;level;thread;file;NDC;logger;properties;message;marked;note");
      String[] chunkOrderArr = chunksOrder.split(";");
      for (String currentChunkOrder : chunkOrderArr) {
        if (currentChunkOrder.contains("date")) {
          //1 date and time
          chunks.add(getDateChunk());
        }
        if (currentChunkOrder.contains("class")) {
          //2 class name
          chunks.add(getClassChunk());
        }
        if (currentChunkOrder.contains("method")) {
          //3 method name
          chunks.add(getMethodChunk());
        }
        if (currentChunkOrder.contains("level")) {
          //4 logger level
          addLevelChunk();
        }
        if (currentChunkOrder.contains("thread")) {
          //5 thread name
          chunks.add(new TextChunkWithStyle("Thread: " + ld.getThread() + NEW_LINE, classMethodStyle));
        }
        if (currentChunkOrder.contains("file")) {
          //6 file
          addFileChunk();
        }
        if (currentChunkOrder.contains("NDC")) {
          //7 NDC
          addNDCChunk();
        }
        if (currentChunkOrder.contains("logger")) {
          //8 logger name
          addLoggerNameChunk();
        }
        if (currentChunkOrder.contains("properties")) {
          //9 properties
          addPropertiesChunk();
        }
        if (currentChunkOrder.contains("message")) {
          //10 message
          addMessageChunk();
          if (cancelStatus.isCancelled()) {
            return chunks;
          }
        }
        if (currentChunkOrder.contains("marked")) {
          //11 marked
          addMarkedChunk();
        }
        if (currentChunkOrder.contains("note")) {
          //12 note
          addNoteChunk();
        }
      }
    }
    return chunks;
  }

  private int countCharsBeforeMessage(ArrayList<TextChunkWithStyle> chunks) {
    int i = 0;
    for (TextChunkWithStyle chunk : chunks) {
      if (chunk.getString() != null) {
        i += chunk.getString().length();
      }
    }
    return ++i;
  }

  private Style getStyleForMarkerColor(MarkerColors markerColors) {
    String styleName = "MarkerColor" + markerColors.name();
    Style style = sc.getStyle(styleName);
    if (style == null) {
      style = sc.addStyle(styleName, mainStyle);
      StyleConstants.setForeground(style, markerColors.getForeground());
      StyleConstants.setBackground(style, markerColors.getBackground());
    }
    return style;
  }

  public int getMaximumMessageSize() {
    return maximumMessageSize;
  }

  public void setMaximumMessageSize(int maximumMessageSize) {
    this.maximumMessageSize = maximumMessageSize;
  }
}
