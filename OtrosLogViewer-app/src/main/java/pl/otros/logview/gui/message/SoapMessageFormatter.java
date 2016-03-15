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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoapMessageFormatter implements MessageFormatter {

  public static final String REMOVE_NIL_PATTERN = "(<\\w+)( [^>]*?xsi:nil=\"true\".*?)(/>)";
  public static final String REMOVE_NIL_REPLACE_PATTERN = "$1$3";
  public static final String XML_VERSION_1_0_ENCODING_UTF_8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  private static final Logger LOGGER = LoggerFactory.getLogger(SoapMessageFormatter.class.getName());
  private static final String NAME = "Soap formatter";
  private static final String DESCRIPTION = "Formatting SOAP messages.";
  private static final Pattern PATTERN_MULTIREF_DEFINITION = Pattern.compile("<(\\w+) href=\"#(id\\d+)\".*?/>", Pattern.DOTALL);
  private static final int PATTERN_MULTIREF_DEFINITION_GROUP_ID = 2;
  private static final int PATTERN_MULTIREF_DEFINITION_GROUP_TAG_NAME = 1;
  private static final Pattern PATTERN_MULTIREF_VALUES = Pattern.compile("<multiRef id=\"(id\\d+)\"(.*?)>(.*?)</multiRef>", Pattern.DOTALL);
  private static final int PATTERN_MULTIREF_VALUES_GROUP_ID = 1;
  private static final int PATTERN_MULTIREF_VALUES_GROUP_SCHEMA_DEF = 2;
  private static final int PATTERN_MULTIREF_VALUES_GROUP_VALUE = 3;
  private final SoapFinder soapFinder = new SoapFinder();
  private boolean removeMultiRefs = false;
  private boolean removeXsiForNilElements;


  /*
       * (non-Javadoc)
       *
       * @see pl.otros.logview.gui.message.MessageFormatter#formattingNeeded(java.lang.String)
       */
  @Override
  public boolean formattingNeeded(String message) {
    return soapFinder.findSoaps(message).size() > 0;
  }

  /*
     * (non-Javadoc)
     *
     * @see pl.otros.logview.gui.message.MessageFormatter#format(java.lang.String, javax.swing.text.StyledDocument)
     */
  @Override
  public String format(String message) {
    StringBuilder sb = new StringBuilder();
    SortedSet<SubText> findSoaps = soapFinder.findSoaps(message);

    int lastEnd = 0;
    for (SubText subText : findSoaps) {
      String subString = message.substring(lastEnd, subText.getStart());
      sb.append(subString);
      sb.append("\n");
      String soapMessage = extractSubText(message, subText);
      if (removeMultiRefs) {
        try {
          soapMessage = removeMultiRefs(soapMessage);
        } catch (Exception e) {
          LOGGER.error("Error occurred when removing multirefs: " + e.getMessage());
        }
      }
      if (removeXsiForNilElements) {
        soapMessage = removeXsiFromNulls(soapMessage);
      }

      try {
        soapMessage = prettyFormat(soapMessage);
      } catch (Exception e) {
        LOGGER.error("Error occurred when formatting soap message: " + e.getMessage());
      }

      sb.append(soapMessage);
      lastEnd = subText.getEnd();
    }
    sb.append(message.substring(lastEnd));
    return sb.toString();
  }


  public String prettyFormat(String input, int indent, boolean omitXmlDeclaration) {
    try {
      Source xmlInput = new StreamSource(new StringReader(input));
      StringWriter stringWriter = new StringWriter();
      StreamResult xmlOutput = new StreamResult(stringWriter);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");

      transformer.transform(xmlInput, xmlOutput);

      String string = xmlOutput.getWriter().toString();
      if (string.startsWith(XML_VERSION_1_0_ENCODING_UTF_8 + "<")) {
        string = XML_VERSION_1_0_ENCODING_UTF_8 + "\n" + string.substring(XML_VERSION_1_0_ENCODING_UTF_8.length());
      }
      return string;
    } catch (Exception e) {
      throw new RuntimeException(e); // simple exception handling, please
      // review it
    }
  }

  public String removeXsiFromNulls(String xml) {
    Pattern p = Pattern.compile(REMOVE_NIL_PATTERN);
    Matcher matcher = p.matcher(xml);
    if (matcher.find()) {
      return matcher.replaceAll(REMOVE_NIL_REPLACE_PATTERN);
    }
    return xml;
  }

  public String removeMultiRefs(String message) {
    HashMap<String, SubText> multiRefDefinition = extractMultiRef(message, PATTERN_MULTIREF_DEFINITION, PATTERN_MULTIREF_DEFINITION_GROUP_ID, 0);
    HashMap<String, SubText> multiRefTagNames = extractMultiRef(message, PATTERN_MULTIREF_DEFINITION, PATTERN_MULTIREF_DEFINITION_GROUP_ID, PATTERN_MULTIREF_DEFINITION_GROUP_TAG_NAME);
    HashMap<String, SubText> multiRefValues = extractMultiRef(message, PATTERN_MULTIREF_VALUES, PATTERN_MULTIREF_VALUES_GROUP_ID, PATTERN_MULTIREF_VALUES_GROUP_VALUE);
    HashMap<String, SubText> multiRefValuesToRemove = extractMultiRef(message, PATTERN_MULTIREF_VALUES, PATTERN_MULTIREF_VALUES_GROUP_ID, 0);
    HashMap<String, SubText> multiRefTagSchemas = extractMultiRef(message, PATTERN_MULTIREF_VALUES, PATTERN_MULTIREF_VALUES_GROUP_ID, PATTERN_MULTIREF_VALUES_GROUP_SCHEMA_DEF);
    StringBuilder sb = new StringBuilder(message);
    Set<String> stringSet = multiRefDefinition.keySet();
    ArrayList<String> sortedKeys = new ArrayList<>(stringSet);
    Collections.sort(sortedKeys);
    for (String id : sortedKeys) {
      //replace reference with value
      SubText subTextDef = multiRefDefinition.get(id);
      String toRemove = extractSubText(message, subTextDef);
      SubText subTextValue = multiRefValues.get(id);
      String replaceWith = extractSubText(message, subTextValue);
      int sbStart = sb.indexOf(toRemove);
      sb.replace(sbStart, sbStart + subTextDef.getLength(), replaceWith);
      //insert Tag
      String tagName = extractSubText(message, multiRefTagNames.get(id));
      sb.insert(sbStart + replaceWith.length(), String.format("</%s>", tagName));
      sb.insert(sbStart, String.format("<%s %s>", tagName, extractSubText(message, multiRefTagSchemas.get(id))));


      //Remove multiref value
      SubText subTextValueToRemove = multiRefValuesToRemove.get(id);
      String sbToRemoveString = extractSubText(message, subTextValueToRemove);
      int sbIndexToRemove = sb.indexOf(sbToRemoveString);
      sb.delete(sbIndexToRemove, sbIndexToRemove + sbToRemoveString.length());

    }
    return sb.toString();

  }

  private String extractSubText(String message, SubText subTextValue) {
    return message.substring(subTextValue.getStart(), subTextValue.getEnd());
  }

  protected HashMap<String, SubText> extractMultiRef(String message, Pattern pattern, int idGroup, int valueGroup) {
    HashMap<String, SubText> multiRefs = new HashMap<>();
    Matcher matcher2 = pattern.matcher(message);
    while (matcher2.find()) {
      String id = matcher2.group(idGroup);
      int start = matcher2.start(valueGroup);
      SubText subText = new SubText(start, matcher2.end(valueGroup));
      multiRefs.put(id, subText);
    }
    return multiRefs;
  }

  public String prettyFormat(String input) {
    int formantIndent = 2;
    return prettyFormat(input, formantIndent, false);
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
    return MESSAGE_FORMATTER_VERSION_1;
  }


  public boolean isRemoveMultiRefs() {
    return removeMultiRefs;
  }

  public void setRemoveMultiRefs(boolean removeMultiRefs) {
    this.removeMultiRefs = removeMultiRefs;
  }

  public boolean isRemoveXsiForNilElements() {
    return removeXsiForNilElements;
  }

  public void setRemoveXsiForNilElements(boolean removeXsiForNilElements) {
    this.removeXsiForNilElements = removeXsiForNilElements;
  }
}
