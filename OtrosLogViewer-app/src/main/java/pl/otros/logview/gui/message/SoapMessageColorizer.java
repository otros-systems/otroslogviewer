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

import pl.otros.logview.api.pluginable.MessageColorizer;
import pl.otros.logview.api.pluginable.MessageFragmentStyle;

import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static pl.otros.logview.gui.message.MessageColorizerUtils.colorizeRegex;

public class SoapMessageColorizer implements MessageColorizer {

  private static final String NAME = "Soap message";

  private final SoapFinder soapFinder = new SoapFinder();

  protected Style styleElementName;
	protected Style styleAttribtuteName;
	protected Style styleAttribtuteValue;
	protected Style styleContent;
	protected Style styleOperator;
	protected Style styleComments;
	protected Style styleCData;
	protected Style styleProcessingInstructions;
	protected Style styleDOCTYPE;

  private static final Pattern pComment = Pattern.compile("(<!--.*?-->)");
  private static final Pattern pContent = Pattern.compile(">(.*?)<");
  private static final Pattern pOperator = Pattern.compile("(<|>|<!--|-->)");
  private static final Pattern pElementName = Pattern.compile("</?([:a-zA-z0-9_-]*)\\s?.*?(>|/>)");
  private static final Pattern pAttributeName = Pattern.compile("\\s+([-\\w\\d_:]*?)=\".*?\"");
  private static final Pattern pAttributeValue = Pattern.compile("\\s+[-\\w\\d_:]*?=(\".*?\")");
  private static final Pattern pCdata = Pattern.compile("(<!\\[CDATA\\[.*?\\]\\]>)");
  private static final Pattern pDoctype = Pattern.compile("(<!doctype.*?>)", Pattern.CASE_INSENSITIVE);
  private static final Pattern pXmlHeader = Pattern.compile("(<\\?xml .*?\\?>)", Pattern.CASE_INSENSITIVE);

  private static final String DESCRIPTION = "Colorize soap message.";

  public SoapMessageColorizer() {
    initStyles();
  }

  private void initStyles() {
    StyleContext sc = new StyleContext();
    Style parent = sc.getStyle(StyleContext.DEFAULT_STYLE);

    StyleConstants.setFontFamily(parent, "courier");
    StyleConstants.setFontSize(parent, 13);

    styleElementName = sc.addStyle("elementName", parent);
    StyleConstants.setForeground(styleElementName, new Color(128, 0, 0));

    styleAttribtuteName = sc.addStyle("attributeName", parent);
    StyleConstants.setForeground(styleAttribtuteName, Color.RED);

    styleAttribtuteValue = sc.addStyle("attributeValue", parent);

    styleContent = sc.addStyle("content", parent);
    StyleConstants.setBackground(styleContent, new Color(200, 255, 100));

    styleOperator = sc.addStyle("operator", parent);
    StyleConstants.setForeground(styleOperator, Color.BLUE);
    StyleConstants.setBold(styleOperator, true);

    styleComments = sc.addStyle("comments", parent);
    StyleConstants.setForeground(styleComments, new Color(128, 128, 128));// Hooker's green

    styleCData = sc.addStyle("cdata", parent);
    StyleConstants.setForeground(styleCData, new Color(30, 30, 0));
    StyleConstants.setBackground(styleCData, new Color(250, 250, 240));

    styleProcessingInstructions = sc.addStyle("processingIntruction", parent);
    styleDOCTYPE = sc.addStyle("doctype", styleComments);
  }

  @Override
  public boolean colorizingNeeded(String message) {
    return soapFinder.findSoaps(message).size() > 0;
  }

  @Override
  public Collection<MessageFragmentStyle> colorize(String message) throws BadLocationException {
		List<MessageFragmentStyle> list = new ArrayList<>();

    SortedSet<SubText> findSoaps = soapFinder.findSoaps(message);
    for (SubText subText : findSoaps) {
      int fragmentStart = subText.getStart();
      int fragmentEnd = subText.getEnd();
      colorizeFragment(message, fragmentStart, fragmentEnd,list);
    }
		return list;
  }

  private void colorizeFragment(String soap, int fragmentStart, int fragmentEnd, Collection<MessageFragmentStyle> list) throws BadLocationException {
    list.addAll(colorizeRegex( styleOperator, soap, fragmentStart, pOperator, 1));
		list.addAll(colorizeRegex( styleContent, soap, fragmentStart, pContent, 1));
		list.addAll(colorizeRegex( styleElementName, soap, fragmentStart, pElementName, 1));
		list.addAll(colorizeRegex( styleComments, soap, fragmentStart, pComment, 1));
		list.addAll(colorizeRegex( styleAttribtuteName, soap, fragmentStart, pAttributeName, 1));
		list.addAll(colorizeRegex( styleAttribtuteValue, soap, fragmentStart, pAttributeValue, 1));
		list.addAll(colorizeRegex( styleComments, soap, fragmentStart, pXmlHeader, 1));
		list.addAll(colorizeRegex( styleCData, soap, fragmentStart, pCdata, 1));
		list.addAll(colorizeRegex( styleDOCTYPE, soap, fragmentStart, pDoctype, 1));
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
