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
import pl.otros.logview.LogData;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.MessageFragmentStyle;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.gui.renderers.LevelRenderer;
import pl.otros.logview.pluginable.PluginableElementsContainer;

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


    public LogDataFormatter(LogData logData, //
                            DateFormat dateFormat,//
                            MessageUpdateUtils messageUtils,//
                            PluginableElementsContainer<MessageColorizer> colorizersContainer,//
                            PluginableElementsContainer<MessageFormatter> formattersContainer,//
                            CancelStatus cancelStatus,int maximumMessageSize) {
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

        propertyNameStyle = sc.addStyle("propertyValue",classMethodStyle);
        StyleConstants.setForeground(propertyNameStyle,new Color(0,0,128));

        propertyValueStyle = sc.addStyle("propertyValue",classMethodStyle);
        StyleConstants.setForeground(propertyNameStyle,new Color(0,128,0));



    }

    public java.util.List<TextChunkWithStyle> format() throws Exception {

        LOGGER.trace("Start do in background");

        String s1 = "Date:    " + dateFormat.format(ld.getDate()) + NEW_LINE;
        chunks.add(new TextChunkWithStyle(s1, mainStyle));
        s1 = "Class:   " + ld.getClazz() + NEW_LINE;
        chunks.add(new TextChunkWithStyle(s1, classMethodStyle));
        s1 = "Method:  " + ld.getMethod() + NEW_LINE;
        chunks.add(new TextChunkWithStyle(s1, classMethodStyle));
        s1 = "Level:   ";
        chunks.add(new TextChunkWithStyle(s1, classMethodStyle));
        Icon levelIcon = LevelRenderer.getIconByLevel(ld.getLevel());
        if (levelIcon != null) {
            chunks.add(new TextChunkWithStyle(levelIcon));
        }
        s1 = " " + ld.getLevel().getName() + NEW_LINE;
        chunks.add(new TextChunkWithStyle(s1, classMethodStyle));

        chunks.add(new TextChunkWithStyle("Thread: " + ld.getThread() + NEW_LINE, classMethodStyle));

        if (StringUtils.isNotBlank(ld.getFile())) {
            s1 = "File: " + ld.getFile();
            if (StringUtils.isNotBlank(ld.getLine())) {
                s1 = s1 + ":" + ld.getLine();
            }
            chunks.add(new TextChunkWithStyle(s1 + NEW_LINE, mainStyle));
        }

        if (StringUtils.isNotBlank(ld.getNDC())) {
            chunks.add(new TextChunkWithStyle("NDC: " + ld.getNDC() + NEW_LINE, mainStyle));
        }

        if (StringUtils.isNotBlank(ld.getLoggerName())) {
            chunks.add(new TextChunkWithStyle("Logger name: " + ld.getLoggerName() + NEW_LINE, mainStyle));
        }

      Map<String, String> properties = ld.getProperties();
      if (properties != null && properties.size() > 0) {
            chunks.add(new TextChunkWithStyle("Properties:\n", boldArialStyle));
          ArrayList<String> keys = new ArrayList(properties.keySet());
          Collections.sort(keys);
          for (String key : keys) {
            chunks.add(new TextChunkWithStyle(key+"=",propertyNameStyle));
            chunks.add(new TextChunkWithStyle(properties.get(key)+"\n",propertyValueStyle));
          }
        }


        s1 = "Message: ";
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
                return chunks;
            }
            s1 = messageUtils.formatMessageWithTimeLimit(s1, messageFormatter, 5);
            s1 = StringUtils.remove(s1,'\r');
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

        chunks.add(new TextChunkWithStyle("\nMarked: ", boldArialStyle));
        if (ld.isMarked()) {
            MarkerColors markerColors = ld.getMarkerColors();
            chunks.add(new TextChunkWithStyle(" " + markerColors.name() + NEW_LINE, getStyleForMarkerColor(markerColors)));
        } else {
            chunks.add(new TextChunkWithStyle("false\n", boldArialStyle));
        }

        Note note = ld.getNote();
        if (note != null && note.getNote() != null && note.getNote().length() > 0) {
            s1 = "Note: " + note.getNote();
            chunks.add(new TextChunkWithStyle(s1, boldArialStyle));
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
