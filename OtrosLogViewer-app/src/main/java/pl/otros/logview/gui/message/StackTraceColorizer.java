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
package pl.otros.logview.gui.message;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.otros.logview.gui.message.MessageColorizerUtils.increaseOffset;

public class StackTraceColorizer implements MessageColorizer {
  private static final String NANE = "Java stack trace";
	protected static final Pattern exceptionLine = Pattern.compile("(\\s*at\\s+([\\w\\d\\.]*)\\.([\\w\\d\\$]+)\\.([\\d\\w<>]+)\\(([\\d\\w\\.\\u0020:]+)\\))");
  protected static final int EXCEPTION_LINE_GROUP_PACKAGE = 2;
  protected static final int EXCEPTION_LINE_GROUP_CLASS = 3;
  protected static final int EXCEPTION_LINE_GROUP_METHOD = 4;
  protected static final int EXCEPTION_LINE_GROUP_FILE = 5;
  private static final String DESCRIPTION = "Colorize java stack trace.";
  private Style styleStackTrace;
  private Style stylePackage;
  private Style styleClass;
  private Style styleMethod;
  private Style styleFile;
  private StackTraceFinder stackTraceFinder;
  private StyleContext styleContext;

  public StackTraceColorizer() {
    stackTraceFinder = new StackTraceFinder();

  }

  protected void initStyles() {
    styleContext = new StyleContext();
    Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
    styleStackTrace = styleContext.addStyle("stackTrace", defaultStyle);
    StyleConstants.setBackground(styleStackTrace, new Color(255, 224, 193));
    StyleConstants.setForeground(styleStackTrace, Color.BLACK);
    StyleConstants.setFontFamily(styleStackTrace, "courier");
    stylePackage = styleContext.addStyle("stylePackage", styleStackTrace);
    styleClass = styleContext.addStyle("styleClass", stylePackage);
    StyleConstants.setForeground(styleClass, new Color(11, 143, 61));
    StyleConstants.setBold(styleClass, true);
    styleMethod = styleContext.addStyle("styleMethod", styleStackTrace);
    StyleConstants.setForeground(styleMethod, new Color(83, 112, 223));
    StyleConstants.setItalic(styleMethod, true);
    StyleConstants.setBold(styleMethod, true);
    styleFile = styleContext.addStyle("styleFile", styleStackTrace);
    StyleConstants.setForeground(styleFile, Color.BLACK);
    StyleConstants.setUnderline(styleFile, true);
  }

  @Override
  public boolean colorizingNeeded(String message) {
    return exceptionLine.matcher(message).find();
  }

  @Override
  public Collection<MessageFragmentStyle> colorize(String message) throws BadLocationException {
    initStyles();
    Collection<MessageFragmentStyle> list = new ArrayList<MessageFragmentStyle>();
    SortedSet<SubText> foundStackTraces = stackTraceFinder.findStackTraces(message);
    for (SubText subText : foundStackTraces) {
      list.add(new MessageFragmentStyle(subText.getStart(), subText.getLength(), styleStackTrace, false));
      String subTextFragment = message.substring(subText.getStart(), subText.getEnd());
      Matcher matcher = exceptionLine.matcher(subTextFragment);
      while (matcher.find()) {
        int newOffset = subText.start;
        LocationInfo locationInfo = getLocationInfo(matcher.group(0));
        list.addAll(addLocation(increaseOffset(MessageColorizerUtils.colorizeRegex(stylePackage, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_PACKAGE), newOffset), locationInfo));
        list.addAll(addLocation(increaseOffset(MessageColorizerUtils.colorizeRegex(styleClass, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_CLASS), newOffset), locationInfo));
        list.addAll(addLocation(increaseOffset(MessageColorizerUtils.colorizeRegex(styleMethod, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_METHOD), newOffset), locationInfo));
        list.addAll(addLocation(increaseOffset(MessageColorizerUtils.colorizeRegex(styleFile, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_FILE), newOffset), locationInfo));
      }
    }
    return list;
  }
  public Collection<MessageFragmentStyle> addLocation(Collection<MessageFragmentStyle> list, LocationInfo locationInfo){
    if (locationInfo == null ){
      return list;
    }
    for (MessageFragmentStyle messageFragmentStyle : list) {
      Style oldStyle = messageFragmentStyle.getStyle();
      String name = oldStyle.getName();
      Style style = styleContext.addStyle(name + "-" + locationInfo.toString(), oldStyle);
      style.addAttribute("locationInfo",locationInfo);
      StyleConstants.setForeground(style,StyleConstants.getForeground(oldStyle));
      StyleConstants.setBold(style,StyleConstants.isBold(oldStyle));
      StyleConstants.setItalic(style,StyleConstants.isItalic(oldStyle));
      messageFragmentStyle.setStyle(style);
    }
    return list;
  }
  protected LocationInfo getLocationInfo(String stackTraceLine) {
    return pl.otros.logview.gui.message.LocationInfo.parse(stackTraceLine);
  }



  @Override
  public String getName() {
    return NANE;
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
