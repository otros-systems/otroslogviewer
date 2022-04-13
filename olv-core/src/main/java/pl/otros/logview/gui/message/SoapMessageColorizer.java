/*
 * ******************************************************************************
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

import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.MessageFragmentStyle;
import pl.otros.logview.api.theme.Theme;
import pl.otros.logview.api.theme.ThemeKey;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Pattern;

import static pl.otros.logview.gui.message.MessageColorizerUtils.colorizeRegex;

public class SoapMessageColorizer implements MessageColorizer {

  private static final String NAME = "Soap message";

  private final SoapFinder soapFinder = new SoapFinder();

  protected Style styleElementName;
  protected Style styleAttributeName;
  protected Style styleAttributeValue;
  protected Style styleContent;
  protected Style styleOperator;
  private Style styleComments;
  private Style styleCData;
  private Style styleDOCTYPE;

  private static final Pattern pComment = Pattern.compile("(<!--.*?-->)");
  private static final Pattern pContent = Pattern.compile(">(.*?)<");
  private static final Pattern pOperator = Pattern.compile("(<|>|<!--|-->)");
  private static final Pattern pElementName = Pattern.compile("</?([:a-zA-z0-9_-]*)\\s?.*?(>|/>)");
  private static final Pattern pAttributeName = Pattern.compile("\\s+([-\\w\\d_:]*?)=\".*?\"");
  private static final Pattern pAttributeValue = Pattern.compile("\\s+[-\\w\\d_:]*?=(\".*?\")");
  private static final Pattern pCdata = Pattern.compile("(<!\\[CDATA\\[.*?]]>)");
  private static final Pattern pDoctype = Pattern.compile("(<!doctype.*?>)", Pattern.CASE_INSENSITIVE);
  private static final Pattern pXmlHeader = Pattern.compile("(<\\?xml .*?\\?>)", Pattern.CASE_INSENSITIVE);

  private static final String DESCRIPTION = "Colorize soap message.";
  private Theme theme;

  public SoapMessageColorizer(Theme theme) {
    this.theme = theme;
    initStyles();
  }

  private void initStyles() {
    StyleContext sc = new StyleContext();
    Style parent = sc.getStyle(StyleContext.DEFAULT_STYLE);

    StyleConstants.setFontFamily(parent, "courier");
    StyleConstants.setFontSize(parent, 13);

    styleElementName = sc.addStyle("elementName", parent);
    StyleConstants.setForeground(styleElementName, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_ELEMENT_NAME));

    styleAttributeName = sc.addStyle("attributeName", parent);
    StyleConstants.setForeground(styleAttributeName, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_ATTRIBUTE_NAME));

    styleAttributeValue = sc.addStyle("attributeValue", parent);
    StyleConstants.setForeground(styleAttributeValue, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_ATTRIBUTE_VALUE));

    styleContent = sc.addStyle("content", parent);
    StyleConstants.setBackground(styleContent, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_CONTENT_BACKGROUND));
    StyleConstants.setForeground(styleContent, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_CONTENT_FOREGROUND));

    styleOperator = sc.addStyle("operator", parent);
    StyleConstants.setForeground(styleOperator, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_OPERATOR));
    StyleConstants.setBold(styleOperator, true);

    styleComments = sc.addStyle("comments", parent);
    StyleConstants.setForeground(styleComments, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_COMMENTS));

    styleCData = sc.addStyle("cdata", parent);
    StyleConstants.setForeground(styleCData, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_CDATA_FOREGROUND));
    StyleConstants.setBackground(styleCData, theme.getColor(ThemeKey.LOG_DETAILS_SOAP_CDATA_BACKGROUND));

    styleDOCTYPE = sc.addStyle("doctype", sc.addStyle("doctype", parent));
  }

  @Override
  public boolean colorizingNeeded(String message) {
    return soapFinder.findSoaps(message).size() > 0;
  }

  @Override
  public Collection<MessageFragmentStyle> colorize(String message) {
    initStyles();
    List<MessageFragmentStyle> list = new ArrayList<>();

    SortedSet<SubText> findSoaps = soapFinder.findSoaps(message);
    for (SubText subText : findSoaps) {
      int fragmentStart = subText.getStart();
      colorizeFragment(message, fragmentStart, list);
    }
    return list;
  }

  private void colorizeFragment(String soap, int fragmentStart, Collection<MessageFragmentStyle> list) {
    list.addAll(colorizeRegex(styleOperator, soap, fragmentStart, pOperator, 1));
    list.addAll(colorizeRegex(styleContent, soap, fragmentStart, pContent, 1));
    list.addAll(colorizeRegex(styleElementName, soap, fragmentStart, pElementName, 1));
    list.addAll(colorizeRegex(styleComments, soap, fragmentStart, pComment, 1));
    list.addAll(colorizeRegex(styleAttributeName, soap, fragmentStart, pAttributeName, 1));
    list.addAll(colorizeRegex(styleAttributeValue, soap, fragmentStart, pAttributeValue, 1));
    list.addAll(colorizeRegex(styleComments, soap, fragmentStart, pXmlHeader, 1));
    list.addAll(colorizeRegex(styleCData, soap, fragmentStart, pCdata, 1));
    list.addAll(colorizeRegex(styleDOCTYPE, soap, fragmentStart, pDoctype, 1));
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
