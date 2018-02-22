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

package pl.otros.logview.gui.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.gui.LogViewPanelI;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.MessageFormatter;
import pl.otros.logview.api.pluginable.MessageFragmentStyle;
import pl.otros.logview.api.pluginable.PluginableElementsContainer;
import pl.otros.logview.gui.message.html.ExportToHtml;
import pl.otros.logview.gui.message.update.CancelStatus;
import pl.otros.logview.gui.message.update.LogDataFormatter;
import pl.otros.logview.gui.message.update.MessageUpdateUtils;
import pl.otros.logview.gui.message.update.TextChunkWithStyle;
import pl.otros.logview.gui.util.ClipboardUtil;
import pl.otros.logview.gui.util.PlainTextAndHtml;

import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 */
public class CopyStyledMessageDetailAction extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(CopyStyledMessageDetailAction.class.getName());

  private final DateFormat dateFormat;
  private final PluginableElementsContainer<MessageColorizer> selectedMessageColorizersContainer;
  private final PluginableElementsContainer<MessageFormatter> selectedMessageFormattersContainer;
  private final ExportToHtml exportToHtml;

  public CopyStyledMessageDetailAction(OtrosApplication otrosApplication, DateFormat dateFormat, PluginableElementsContainer<MessageColorizer> selectedMessageColorizersContainer, PluginableElementsContainer<MessageFormatter> selectedMessageFormattersContainer) {
    super(otrosApplication);
    this.dateFormat = dateFormat;
    this.selectedMessageColorizersContainer = selectedMessageColorizersContainer;
    this.selectedMessageFormattersContainer = selectedMessageFormattersContainer;
    putValue(NAME, "Copy message detail [styled text]");
    putValue(SMALL_ICON, Icons.DOCUMENT_COPY);

    exportToHtml = new ExportToHtml();
  }

  @Override
  protected void actionPerformedHook(ActionEvent e) {
    MessageUpdateUtils messageUpdateUtils = new MessageUpdateUtils();
    Optional<LogViewPanelI> selectedLogViewPanel1 = getOtrosApplication().getSelectedLogViewPanel();
    selectedLogViewPanel1.ifPresent(selectedLogViewPanel -> {
      if (selectedLogViewPanel.getDisplayedLogData() == null) {
        LOGGER.debug("Currently no LogData is displayed, nothing to copy");
        return;
      }
      LogData logData = selectedLogViewPanel.getDisplayedLogData();
      PlainTextAndHtml plainTextAndHtml = convertToHtml(messageUpdateUtils, logData);
      ClipboardUtil.copyToClipboard(plainTextAndHtml);
    });
  }

  private PlainTextAndHtml convertToHtml(MessageUpdateUtils messageUpdateUtils, LogData logData) {
    final long start = System.currentTimeMillis();
    CancelStatus cancelStatus = () -> {
      boolean b = start > System.currentTimeMillis() + 5000;
      LOGGER.debug("Is cancelled: " + b);
      return b;
    };
    PlainTextAndHtml plainTextAndHtml = new PlainTextAndHtml();
    LogDataFormatter logDataFormatter = new LogDataFormatter(getOtrosApplication(), logData, dateFormat, messageUpdateUtils, selectedMessageColorizersContainer, selectedMessageFormattersContainer, cancelStatus, 500 * 1000);
    try {
      List<TextChunkWithStyle> format = logDataFormatter.format();
      StringBuilder sb = new StringBuilder();
      ArrayList<MessageFragmentStyle> styleArrayList = new ArrayList<>(format.size());
      for (TextChunkWithStyle textChunkWithStyle : format) {
        if (textChunkWithStyle.getString() != null) {
          sb.append(textChunkWithStyle.getString());
        }
        if (textChunkWithStyle.getMessageFragmentStyle() != null) {
          styleArrayList.add(textChunkWithStyle.getMessageFragmentStyle());
        }
      }
      String title = String.format("Log event [id: %d] at %s from %s", logData.getId(), logData.getDate(), logData.getLogSource());
      String plainText = sb.toString();
      String html = exportToHtml.format(sb.toString(), styleArrayList, title, ExportToHtml.HTML_MODE.INLINE_HTML);
      plainTextAndHtml.setPlainText(plainText);
      plainTextAndHtml.setHtml(html);
    } catch (Exception e) {
      LOGGER.error("Error occurred when formatting message", e);
    }
    return plainTextAndHtml;
  }

}
