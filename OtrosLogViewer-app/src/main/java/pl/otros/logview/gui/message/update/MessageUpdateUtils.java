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
import org.apache.commons.lang.exception.ExceptionUtils;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.message.MessageFragmentStyle;
import pl.otros.logview.gui.message.SearchResultColorizer;
import pl.otros.logview.pluginable.PluginableElementsContainer;
import pl.otros.swing.rulerbar.OtrosJTextWithRulerScrollPane;
import pl.otros.swing.rulerbar.RulerBarHelper;
import pl.otros.vfs.browser.ExceptionsUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class MessageUpdateUtils {

  public static final Logger LOGGER = Logger.getLogger(MessageUpdateUtils.class.getName());

  private ExecutorService executorService;

  public MessageUpdateUtils() {
    executorService = Executors.newSingleThreadExecutor();
  }


  public String formatMessageWithTimeLimit(final String s1, final MessageFormatter messageFormatter, int timeoutSeconds) {
    String result = s1;
    Callable<String> callable = new Callable<String>() {


      @Override
      public String call() throws Exception {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(messageFormatter.getClass().getClassLoader());
          if (messageFormatter.formattingNeeded(s1)) {
            return messageFormatter.format(s1);
          }
        } catch (Throwable e) {
          LOGGER.severe(String.format("Error occurred when using message formatter %s: %s", messageFormatter.getName(), e.getMessage()));
          LOGGER.fine(String.format("Error occurred when using message formatter %s with message\"%s\"", messageFormatter.getName(), StringUtils.left(s1, 1500)));
        } finally {
          Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return s1;
      }
    };


    Future<String> submit = executorService.submit(callable);
    try {
      result = submit.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      String msg = String.format("Formatting message with %s takes to long time, skipping this formatter", messageFormatter.getName());
      LOGGER.warning(msg);
      submit.cancel(true);
    } catch (Exception e) {
      LOGGER.severe(String.format("Error occurred when using message formatter %s: %s", messageFormatter.getName(), e.getMessage()));
      submit.cancel(true);
    }

    return result;
  }

  public Collection<MessageFragmentStyle> colorizeMessageWithTimeLimit(final String message, final int messageStartOffset, final MessageColorizer messageColorizer, int timeoutSeconds) {

    Callable<Collection<MessageFragmentStyle>> callable = new Callable<Collection<MessageFragmentStyle>>() {
      @Override
      public Collection<MessageFragmentStyle> call() throws Exception {
        Collection<MessageFragmentStyle> list = new ArrayList<MessageFragmentStyle>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
          Thread.currentThread().setContextClassLoader(messageColorizer.getClass().getClassLoader());
          if (messageColorizer.colorizingNeeded(message)) {
            Collection<MessageFragmentStyle> colorize = messageColorizer.colorize(message);
            for (MessageFragmentStyle messageFragmentStyle : colorize) {
              messageFragmentStyle.setOffset(messageFragmentStyle.getOffset() + messageStartOffset);
            }
            list.addAll(colorize);
          }
        } catch (Throwable e) {
          LOGGER.log(Level.SEVERE,String.format("Error occurred when using message colorizer %s: %s%n%s", messageColorizer.getName(), e.getMessage()),e);
          LOGGER.fine(String.format("Error occurred when using message colorizer %s with message\"%s\"", messageColorizer.getName(), StringUtils.left(message, 1500)));
          e.printStackTrace();
        } finally {
          Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return list;
      }
    };

    Future<Collection<MessageFragmentStyle>> submit = executorService.submit(callable);
    try {
      return submit.get(timeoutSeconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      String msg = String.format("Formatting message with %s takes to long time, skipping this formatter", messageColorizer.getName());
      LOGGER.warning(msg);
      submit.cancel(true);
    } catch (Exception e) {
      LOGGER.severe(String.format("Error occurred when using message formatter %s: %s", messageColorizer.getName(), e.getMessage()));
      submit.cancel(true);
    }
    return new ArrayList<MessageFragmentStyle>(0);
  }


  private static void markSearchResult(List<MessageFragmentStyle> searchResultPositions, OtrosJTextWithRulerScrollPane<? extends
      JTextComponent> otrosJTextWithRulerScrollPane) {
    RulerBarHelper.clearMarkers(otrosJTextWithRulerScrollPane);
    otrosJTextWithRulerScrollPane.getjTextComponent().getHighlighter().removeAllHighlights();
    Highlighter.HighlightPainter highlighter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

    for (MessageFragmentStyle mfs : searchResultPositions) {
      int position = mfs.getOffset();
      RulerBarHelper.addTextMarkerToPosition(otrosJTextWithRulerScrollPane, position, "Search result",
          Color.YELLOW.darker().darker().darker(),
          RulerBarHelper.TooltipMode.LINE_NUMBER_PREFIX);
      try {
        otrosJTextWithRulerScrollPane.getjTextComponent().getHighlighter().addHighlight(mfs.getOffset(), mfs.getLength() + mfs.getOffset(),
            highlighter);
      } catch (BadLocationException e) {
        LOGGER.log(Level.SEVERE, "Cant get text of log detail view for highlighting search result", e);
      }
    }
    LOGGER.finest("Update with chunks finished");
  }

  public static void highlightSearchResult(OtrosJTextWithRulerScrollPane<JTextPane> otrosJTextWithRulerScrollPane,
                                           PluginableElementsContainer<MessageColorizer> colorizersContainer) {
    MessageUpdateUtils messageUpdateUtils = new MessageUpdateUtils();
    StyledDocument styledDocument = otrosJTextWithRulerScrollPane.getjTextComponent().getStyledDocument();
    String text;
    try {
      text = styledDocument.getText(0, styledDocument.getLength());
    } catch (BadLocationException e) {
      LOGGER.log(Level.SEVERE, "Cant get document text for log details view: ", e);
      return;
    }
    MessageColorizer messageColorizer = colorizersContainer.getElement(SearchResultColorizer.class.getName());
    List<MessageFragmentStyle> messageFragmentStyles = new ArrayList<MessageFragmentStyle>();
    if (messageColorizer != null) {
      messageFragmentStyles.addAll(messageUpdateUtils.colorizeMessageWithTimeLimit(text, 0, messageColorizer, 10));
    }
    markSearchResult(messageFragmentStyles, otrosJTextWithRulerScrollPane);
  }


}
