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
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.api.theme.ThemeKey;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.gui.renderers.LevelRenderer;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.text.DateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 */
public class LogDataFormatter {

  private static final String NEW_LINE = "\n";
  private static final Logger LOGGER = LoggerFactory.getLogger(LogDataFormatter.class.getName());

  private final StyleContext sc;
  private final PluginableElementsContainer<MessageColorizer> colorizersContainer;
  private final PluginableElementsContainer<MessageFormatter> formattersContainer;
  private final CancelStatus cancelStatus;
  private final LogData ld;
  private final ArrayList<TextChunkWithStyle> chunks = new ArrayList<>();
  private final DateFormat dateFormat;

  private Style mainStyle;
  private Style propertyStyle;
  private Style messageStyle;
  private Style messageMonospacedStyle;
  private Style valueStyle;
  private Style propertyNameStyle;
  private Style propertyValueStyle;
  private int maximumMessageSize;


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
    final Theme theme = otrosApplication.getTheme();

    sc = new StyleContext();
    Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
    mainStyle = sc.addStyle("MainStyle", defaultStyle);
    StyleConstants.setForeground(mainStyle, theme.getColor(ThemeKey.LOG_DETAILS_DEFAULT));

    messageStyle = sc.addStyle("message", defaultStyle);
    StyleConstants.setForeground(messageStyle, theme.getColor(ThemeKey.LOG_DETAILS_MESSAGE));

    messageMonospacedStyle = sc.addStyle("messageMonospaced", messageStyle);
    StyleConstants.setFontFamily(messageMonospacedStyle, "monospaced");
    StyleConstants.setForeground(messageMonospacedStyle, theme.getColor(ThemeKey.LOG_DETAILS_MESSAGE));

    valueStyle = sc.addStyle("valueStyle", null);
    StyleConstants.setFontFamily(valueStyle, "monospaced");
    StyleConstants.setForeground(valueStyle, theme.getColor(ThemeKey.LOG_DETAILS_VALUE));

    propertyStyle = sc.addStyle("property", null);
    StyleConstants.setFontFamily(propertyStyle, "monospaced");
    StyleConstants.setForeground(propertyStyle, theme.getColor(ThemeKey.LOG_DETAILS_PROPERTY));

    propertyNameStyle = sc.addStyle("propertyName", valueStyle);
    final Color propValueColor = theme.getColor(ThemeKey.LOG_DETAILS_PROPERTY_KEY);
    StyleConstants.setForeground(propertyNameStyle, propValueColor);

    propertyValueStyle = sc.addStyle("propertyValue", valueStyle);
    final Color color = theme.getColor(ThemeKey.LOG_DETAILS_PROPERTY_VALUE);
    StyleConstants.setForeground(propertyValueStyle, color);


  }

  private Collection<TextChunkWithStyle> getDateChunk() {
    return Arrays.asList(
      new TextChunkWithStyle("Date:    ", propertyStyle),
      new TextChunkWithStyle(dateFormat.format(ld.getDate()) + NEW_LINE, valueStyle)
    );
  }

  private Collection<TextChunkWithStyle> getClassChunk() {
    return Arrays.asList(
      new TextChunkWithStyle("Class:   ", propertyStyle),
      new TextChunkWithStyle(ld.getClazz() + NEW_LINE, valueStyle));
  }

  private Collection<TextChunkWithStyle> getMethodChunk() {
    String s1 = ld.getMethod() + NEW_LINE;
    return Arrays.asList(new TextChunkWithStyle("Method:  ", propertyStyle), new TextChunkWithStyle(s1, valueStyle));
  }

  private void addLevelChunk() {
    String s1 = "Level:   ";
    chunks.add(new TextChunkWithStyle(s1, propertyStyle));
    Icon levelIcon = LevelRenderer.getIconByLevel(ld.getLevel());
    if (levelIcon != null) {
      chunks.add(new TextChunkWithStyle(levelIcon));
    }
    s1 = " " + Optional.ofNullable(ld.getLevel()).map(Level::getName).orElse("") + NEW_LINE;
    chunks.add(new TextChunkWithStyle(s1, valueStyle));
  }

  private void addFileChunk() {
    if (StringUtils.isNotBlank(ld.getFile())) {
      chunks.add(new TextChunkWithStyle("File:    ", propertyStyle));
      chunks.add(new TextChunkWithStyle(ld.getFile(), valueStyle));
      if (StringUtils.isNotBlank(ld.getLine())) {
        chunks.add(new TextChunkWithStyle(":" + ld.getLine(), valueStyle));
      }
      chunks.add(new TextChunkWithStyle(NEW_LINE, valueStyle));
    }
  }

  private void addNDCChunk() {
    if (StringUtils.isNotBlank(ld.getNDC())) {
      chunks.add(new TextChunkWithStyle("NDC: ", propertyStyle));
      chunks.add(new TextChunkWithStyle(ld.getNDC() + NEW_LINE, mainStyle));
    }
  }

  private void addLoggerNameChunk() {
    if (StringUtils.isNotBlank(ld.getLoggerName())) {
      chunks.add(new TextChunkWithStyle("Logger:  ", propertyStyle));
      chunks.add(new TextChunkWithStyle(ld.getLoggerName() + NEW_LINE, valueStyle));
    }
  }

  private void addPropertiesChunk() {
    Map<String, String> properties = ld.getProperties();
    if (properties != null && properties.size() > 0) {
      chunks.add(new TextChunkWithStyle("Properties:\n", propertyStyle));
      properties
        .keySet()
        .stream()
        .sorted()
        .forEach(key -> {
          chunks.add(new TextChunkWithStyle(" * " + key + "=", propertyNameStyle));
          chunks.add(new TextChunkWithStyle(properties.get(key) + "\n", propertyValueStyle));
        });
    }
  }

  private void addMessageChunk() {
    Boolean useMonospaceFont = otrosApplication.getConfiguration().getBoolean(ConfKeys.MESSAGE_FORMATTER_USE_MONOSPACE_FONT_IN_MESSAGE_CHUNK,
      false);

    chunks.add(new TextChunkWithStyle("\nMessage: ", propertyStyle));
    String s1 = ld.getMessage();
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
    if (useMonospaceFont) {
      chunks.add(new TextChunkWithStyle(s1, messageMonospacedStyle));
    } else {
      chunks.add(new TextChunkWithStyle(s1, messageStyle));
    }


    Collection<MessageColorizer> colorizers = colorizersContainer.getElements();
    ArrayList<MessageFragmentStyle> messageFragmentStyles = new ArrayList<>();
    for (MessageColorizer messageColorizer : colorizers) {
      if (messageColorizer.getPluginableId().equals(SearchResultColorizer.class.getName())) {
        continue;
      }
      messageFragmentStyles.addAll(messageUtils.colorizeMessageWithTimeLimit(s1, charsBeforeMessage, messageColorizer, 5));
    }

    chunks.addAll(messageFragmentStyles.stream().map(
      messageFragmentStyle -> new TextChunkWithStyle(null, messageFragmentStyle)).collect(Collectors.toList()));

  }

  private void addMarkedChunk() {
    chunks.add(new TextChunkWithStyle("\nMarked: ", propertyStyle));
    if (ld.isMarked()) {
      MarkerColors markerColors = ld.getMarkerColors();
      chunks.add(new TextChunkWithStyle(" " + markerColors.name() + NEW_LINE, getStyleForMarkerColor(markerColors)));
    } else {
      chunks.add(new TextChunkWithStyle("false\n", propertyValueStyle));
    }
  }

  private void addNoteChunk() {
    Note note = ld.getNote();
    if (note != null && note.getNote() != null && note.getNote().length() > 0) {
      chunks.add(new TextChunkWithStyle("Note: ", propertyStyle));
      chunks.add(new TextChunkWithStyle(note.getNote(), messageStyle));
    }

  }

  public List<TextChunkWithStyle> format() {

    LOGGER.trace("Start do in background");
    if (otrosApplication.getConfiguration() != null) {
      //try to get chunks order and set default if not in config
      String chunksOrder = otrosApplication.getConfiguration().getString(ConfKeys.MESSAGE_FORMATTER_CHUNKS_ORDER,
        "date;class;method;level;thread;file;NDC;logger;properties;message;marked;note");
      String[] chunkOrderArr = chunksOrder.split(";");
      for (String currentChunkOrder : chunkOrderArr) {
        if (currentChunkOrder.contains("date")) {
          //1 date and time
          chunks.addAll(getDateChunk());
        } else if (currentChunkOrder.contains("class")) {
          //2 class name
          chunks.addAll(getClassChunk());
        } else if (currentChunkOrder.contains("method")) {
          //3 method name
          chunks.addAll(getMethodChunk());
        } else if (currentChunkOrder.contains("level")) {
          //4 logger level
          addLevelChunk();
        } else if (currentChunkOrder.contains("thread")) {
          //5 thread name
          chunks.add(new TextChunkWithStyle("Thread:  ", propertyStyle));
          chunks.add(new TextChunkWithStyle(ld.getThread() + NEW_LINE, valueStyle));
        } else if (currentChunkOrder.contains("file")) {
          //6 file
          addFileChunk();
        } else if (currentChunkOrder.contains("NDC")) {
          //7 NDC
          addNDCChunk();
        } else if (currentChunkOrder.contains("logger")) {
          //8 logger name
          addLoggerNameChunk();
        } else if (currentChunkOrder.contains("properties")) {
          //9 properties
          addPropertiesChunk();
        } else if (currentChunkOrder.contains("message")) {
          //10 message
          addMessageChunk();
          if (cancelStatus.isCancelled()) {
            return chunks;
          }
        } else if (currentChunkOrder.contains("marked")) {
          //11 marked
          addMarkedChunk();
        } else if (currentChunkOrder.contains("note")) {
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

}
