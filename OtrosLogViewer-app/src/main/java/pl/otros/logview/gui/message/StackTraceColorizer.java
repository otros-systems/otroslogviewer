/*******************************************************************************
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
package pl.otros.logview.gui.message;

import pl.otros.logview.api.model.LocationInfo;
import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.MessageFragmentStyle;
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.api.theme.ThemeKey;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StackTraceColorizer implements MessageColorizer {
  private static final String NAME = "Java stack trace";
  protected static final Pattern exceptionLine = Pattern.compile("(\\s*at\\s+([\\w\\d\\.]*)\\.([\\w\\d\\$]+)\\.([\\d\\w<>]+)\\(([\\d\\w\\.\\u0020:]+)\\)(\\s*//.*)?)");
  protected static final int EXCEPTION_LINE_GROUP_PACKAGE = 2;
  protected static final int EXCEPTION_LINE_GROUP_CLASS = 3;
  protected static final int EXCEPTION_LINE_GROUP_METHOD = 4;
  protected static final int EXCEPTION_LINE_GROUP_FILE = 5;
  protected static final int EXCEPTION_LINE_GROUP_CODE_COMMENT = 6;

  protected static final Pattern exceptionNameAndMessage = Pattern.compile("(((Caused by:)|(\\S+\\.)).*(Exception|Error):.*)");

  private static final String DESCRIPTION = "Colorize java stack trace.";
  public static final String STYLE_ATTRIBUTE_EXCEPTION_MSG = "exceptionMessage";
  public static final String STYLE_ATTRIBUTE_LOCATION_INFO = "locationInfo";
  private Style styleStackTrace;
  private Style stylePackage;
  private Style styleClass;
  private Style styleMethod;
  private Style styleFile;
  private Style styleCodeComment;
  private final StackTraceFinder stackTraceFinder;
  private StyleContext styleContext;
  private Theme theme;

  public StackTraceColorizer(Theme theme) {
    this.theme = theme;
    stackTraceFinder = new StackTraceFinder();
  }

  protected void initStyles() {
    styleContext = new StyleContext();
    Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setFontFamily(defaultStyle, "courier");
    styleStackTrace = styleContext.addStyle("stackTrace", defaultStyle);

    StyleConstants.setBackground(styleStackTrace, theme.getColor(ThemeKey.LOG_DETAILS_STACKTRACE_BACKGROUND));
    StyleConstants.setForeground(styleStackTrace, theme.getColor(ThemeKey.LOG_DETAILS_STACKTRACE_FOREGROUND));
    stylePackage = styleContext.addStyle("stylePackage", styleStackTrace);
    styleClass = styleContext.addStyle("styleClass", stylePackage);
    StyleConstants.setForeground(styleClass, theme.getColor(ThemeKey.LOG_DETAILS_STACKTRACE_CLASS));
    StyleConstants.setBold(styleClass, true);
    styleMethod = styleContext.addStyle("styleMethod", styleStackTrace);
    StyleConstants.setForeground(styleMethod, theme.getColor(ThemeKey.LOG_DETAILS_STACKTRACE_METHOD));
    StyleConstants.setItalic(styleMethod, true);
    StyleConstants.setBold(styleMethod, true);
    styleFile = styleContext.addStyle("styleFile", styleStackTrace);
    StyleConstants.setForeground(styleFile, theme.getColor(ThemeKey.LOG_DETAILS_STACKTRACE_FLE));
    StyleConstants.setUnderline(styleFile, true);

    styleCodeComment = styleContext.addStyle("styleCodeComment", defaultStyle);
    StyleConstants.setForeground(styleCodeComment, theme.getColor(ThemeKey.LOG_DETAILS_STACKTRACE_COMMENT));
    StyleConstants.setItalic(styleCodeComment, true);
  }

  @Override
  public boolean colorizingNeeded(String message) {
    return exceptionLine.matcher(message).find();
  }

  @Override
  public Collection<MessageFragmentStyle> colorize(String message) throws BadLocationException {
    initStyles();
    Collection<MessageFragmentStyle> list = new ArrayList<>();
    SortedSet<SubText> foundStackTraces = stackTraceFinder.findStackTraces(message);
    for (SubText subText : foundStackTraces) {
      list.add(new MessageFragmentStyle(subText.getStart(), subText.getLength(), styleStackTrace, false));
      String subTextFragment = message.substring(subText.getStart(), subText.getEnd());
      Matcher matcher = exceptionLine.matcher(subTextFragment);
      int newOffset = subText.getStart();

      while (matcher.find()) {
        ArrayList<MessageFragmentStyle> list1= new ArrayList<>();
        list1.addAll(colorizeStackTraceRegex(styleClass, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_CLASS));
        list1.addAll(colorizeStackTraceRegex(stylePackage, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_PACKAGE));
        list1.addAll(colorizeStackTraceRegex(styleMethod, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_METHOD));
        list1.addAll(colorizeStackTraceRegex(styleFile, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_FILE));
        list1.addAll(colorizeStackTraceRegex(styleCodeComment, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_CODE_COMMENT));
        list1.stream().map(msf->msf.shift(newOffset)).collect(Collectors.toCollection(()->list));
      }
      final Collection<MessageFragmentStyle> exceptionNameAndMessage = findExceptionNameAndMessage(styleStackTrace, subTextFragment);
      exceptionNameAndMessage.stream().map(m->m.shift(newOffset)).collect(Collectors.toCollection(()->list));
    }
    return list;
  }

  protected Collection<MessageFragmentStyle> findExceptionNameAndMessage(final Style style, String subTextFragment) {
    final ArrayList<MessageFragmentStyle> result = new ArrayList<>();
    Matcher matcherMessage = exceptionNameAndMessage.matcher(subTextFragment);
    while (matcherMessage.find()) {
      final int beginIndex = matcherMessage.start(1);
      final int endIndex = matcherMessage.end(1);
      final String msg = subTextFragment.substring(beginIndex, endIndex).replaceFirst("Caused by: ", "");
      final Style style1 = styleContext.addStyle("exceptionMessage-" + msg, style);
      style1.addAttribute(STYLE_ATTRIBUTE_EXCEPTION_MSG, msg);
      result.add(new MessageFragmentStyle(beginIndex, endIndex - beginIndex, style1, false));
      System.out.println("Setting style with exceptionMessage " + style1);
    }
    return result;
  }

  protected Collection<MessageFragmentStyle> colorizeStackTraceRegex(final Style style, String text, Pattern regex, int group) {
    ArrayList<MessageFragmentStyle> list = new ArrayList<>();
    Matcher matcher = regex.matcher(text);
    Style styleToUse = style;
    while (matcher.find()) {
      LocationInfo locationInfo = LocationInfo.parse(matcher.group(0));
      if (locationInfo != null) {
        String name = styleToUse.getName();
        Style newStyle = styleContext.addStyle(name + "-" + locationInfo.toString(), styleToUse);
        newStyle.addAttribute(STYLE_ATTRIBUTE_LOCATION_INFO, locationInfo);
        StyleConstants.setForeground(newStyle, StyleConstants.getForeground(styleToUse));
        StyleConstants.setBold(newStyle, StyleConstants.isBold(styleToUse));
        StyleConstants.setItalic(newStyle, StyleConstants.isItalic(styleToUse));
        styleToUse = newStyle;
      }
      int start = matcher.start(group);
      int end = matcher.end(group);
      if (end - start > 0) {
        MessageFragmentStyle messageFragmentStyle = new MessageFragmentStyle(start, end - start, styleToUse, false);
        list.add(messageFragmentStyle);
      }
    }
    return list;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public String getPluginableId() {
    return this.getClass().getName();
  }

  @Override
  public int getApiVersion() {
    return MESSAGE_COLORIZER_VERSION_CURRENT;
  }
}
