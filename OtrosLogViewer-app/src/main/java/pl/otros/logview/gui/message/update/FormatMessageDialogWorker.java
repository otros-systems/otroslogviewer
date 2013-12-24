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

import pl.otros.logview.LogData;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.MessageFragmentStyle;
import pl.otros.logview.pluginable.PluginableElementsContainer;
import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;
import pl.otros.swing.rulerbar.RulerBarHelper;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class FormatMessageDialogWorker extends SwingWorker<List<TextChunkWithStyle>, TextChunkWithStyle> implements CancelStatus {

  private static final Logger LOGGER = Logger.getLogger(FormatMessageDialogWorker.class.getName());

  private LogData ld;
  private ArrayList<TextChunkWithStyle> chunks = new ArrayList<TextChunkWithStyle>();
  private SimpleDateFormat dateFormat;

  private OtrosJTextWithRulerScrollPane<JTextPane> otrosJTextWithRulerScrollPane;
  private final PluginableElementsContainer<MessageColorizer> colorizersContainer;
  private final PluginableElementsContainer<MessageFormatter> formattersContainer;
  private MessageUpdateUtils messageUtils;
  private int maximumMessageSize;


  public FormatMessageDialogWorker(LogData logData, //
                                   SimpleDateFormat dateFormat,//
                                   OtrosJTextWithRulerScrollPane<JTextPane> otrosJTextWithRulerScrollPane,//
                                   PluginableElementsContainer<MessageColorizer> colorizersContainer,//
                                   PluginableElementsContainer<MessageFormatter> formattersContainer, int maximumMessageSize) {
    this.ld = logData;
    this.dateFormat = dateFormat;
    this.otrosJTextWithRulerScrollPane = otrosJTextWithRulerScrollPane;
    this.colorizersContainer = colorizersContainer;
    this.formattersContainer = formattersContainer;
    this.maximumMessageSize = maximumMessageSize;


    messageUtils = new MessageUpdateUtils();

  }

  @Override
  protected List<TextChunkWithStyle> doInBackground() throws Exception {
    LOGGER.finer("Start do in background");
    LogDataFormatter logDataFormatter = new LogDataFormatter(ld, dateFormat, messageUtils, colorizersContainer, formattersContainer, this, maximumMessageSize);
    chunks.addAll(logDataFormatter.format());
    return chunks;
  }


  @Override
  protected void done() {
    LOGGER.finest("Message details format and colors calculated, updating GUI");
    StyledDocument styledDocument = otrosJTextWithRulerScrollPane.getjTextComponent().getStyledDocument();
    try {
      styledDocument.remove(0, styledDocument.getLength());
    } catch (BadLocationException e) {
      LOGGER.log(Level.SEVERE, "Can't clear log events text  area", e);
    }
    if (!isCancelled()) {
      updateChanges(chunks);
    }
    LOGGER.finest("GUI updated");
  }

  protected void updateChanges(List<TextChunkWithStyle> chunks) {
    LOGGER.finest("Start updating view with chunks, size: " + chunks.size());
    StyledDocument document = otrosJTextWithRulerScrollPane.getjTextComponent().getStyledDocument();
    List<MessageFragmentStyle> searchResultPositions = new ArrayList<MessageFragmentStyle>();
    int i = 0;
    for (TextChunkWithStyle chunk : chunks) {
      LOGGER.finer("Updating with chunk " + i++);
      try {

        if (chunk.getString() != null) {
          if (chunk.getMessageFragmentStyle() != null) {
            document.insertString(document.getLength(), chunk.getString(), chunk.getMessageFragmentStyle().getStyle());
          } else {
            document.insertString(document.getLength(), chunk.getString(), chunk.getStyle());
          }

        } else if (chunk.getMessageFragmentStyle() != null) {
          MessageFragmentStyle mfs = chunk.getMessageFragmentStyle();
          document.setCharacterAttributes(mfs.getOffset(), mfs.getLength(), mfs.getStyle(), mfs.isReplace());
        }
        if (chunk.getIcon() != null) {
          otrosJTextWithRulerScrollPane.getjTextComponent().insertIcon(chunk.getIcon());
        }
      } catch (BadLocationException e) {
        LOGGER.log(Level.SEVERE, "Can't update log details text area", e);
      }
    }

    otrosJTextWithRulerScrollPane.getjTextComponent().setCaretPosition(0);
    MessageUpdateUtils.highlightSearchResult(otrosJTextWithRulerScrollPane, colorizersContainer);
    RulerBarHelper.scrollToFirstMarker(otrosJTextWithRulerScrollPane);
  }


}
