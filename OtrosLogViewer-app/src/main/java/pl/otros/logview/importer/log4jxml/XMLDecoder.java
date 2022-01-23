/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.otros.logview.importer.log4jxml;

import org.apache.logging.log4j.Level;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import pl.otros.logview.parser.log4j.LocationInfo;
import pl.otros.logview.parser.log4j.LoggingEvent;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decodes Logging Events in XML formated into elements that are used by Chainsaw.
 * <p>
 * This decoder can process a collection of log4j:event nodes ONLY (no XML declaration nor eventSet node)
 * <p>
 * NOTE: Only a single LoggingEvent is returned from the decode method even though the DTD supports multiple events nested in an eventSet.
 *
 * @author Scott Deboy (sdeboy@apache.org)
 * @author Paul Smith (psmith@apache.org)
 * @since 1.3
 */
public class XMLDecoder {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(XMLDecoder.class);
  /**
   * Document prolog.
   */
  private static final String BEGINPART = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<!DOCTYPE log4j:eventSet SYSTEM \"http://localhost/log4j.dtd\">"
    + "<log4j:eventSet version=\"1.2\" " + "xmlns:log4j=\"http://jakarta.apache.org/log4j/\">";
  /**
   * Document close.
   */
  private static final String ENDPART = "</log4j:eventSet>";
  /**
   * Record end.
   */
  private static final String RECORD_END = "</log4j:event>";

  
  private static final Pattern KEY_OR_EOL_PATTERN = Pattern.compile("(\\s\\w+=|$)");

  /**
   * XML 1.0 valid characters: 0x9 | 0xA | 0xD | 0x20-0xD7FF | 0xE000-0xFFFD | 0x10000-0x10FFFF
   */
  private static final String INVALID_XML_CHARS = "[^" + "\u0009\r\n" + "\u0020-\uD7FF" + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";
  /**
   * Used to replace an unknown, unrecognized or unrepresentable character<br/>
   * http://www.unicode.org/charts/PDF/UFFF0.pdf
   */
  private static final String REPLACEMENT_CHARACTER = "\uFFFD";
  private static final Pattern INVALID_XML_CHARS_PATTERN = Pattern.compile(INVALID_XML_CHARS);

  /**
   * Document builder.
   */
  private DocumentBuilder docBuilder;
  /**
   * Additional properties.
   */
  private Map additionalProperties = new HashMap();
  /**
   * Partial event.
   */
  private String partialEvent = "";
  /**
   * Owner.
   */
  private Component owner = null;

  /**
   * Create new instance.
   *
   * @param o owner
   */
  public XMLDecoder(final Component o) {
    this();
    this.owner = o;
  }

  /**
   * Create new instance.
   */
  public XMLDecoder() {
    super();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);

    try {
      docBuilder = dbf.newDocumentBuilder();
      docBuilder.setErrorHandler(new SAXErrorHandler());
//      docBuilder.setEntityResolver(new Log4jEntityResolver());
    } catch (ParserConfigurationException pce) {
      System.err.println("Unable to get document builder");
    }
  }

  /**
   * Sets an additionalProperty map, where each Key/Value pair is automatically added to each LoggingEvent as it is decoded.
   * <p>
   * This is useful, say, to include the source file name of the Logging events
   *
   * @param properties additional properties
   */
  public void setAdditionalProperties(final Map properties) {
    this.additionalProperties = properties;
  }

  /**
   * Converts the LoggingEvent data in XML string format into an actual XML Document class instance.
   *
   * @param data XML fragment
   * @return dom document
   */
  private Document parse(final String data) {
    if (docBuilder == null || data == null) {
      return null;
    }

    String buf = null;
    try {
      // we change the system ID to a valid URI so that Crimson won't
      // complain. Indeed, "log4j.dtd" alone is not a valid URI which
      // causes Crimson to barf. The Log4jEntityResolver only cares
      // about the "log4j.dtd" ending.
      buf = BEGINPART + data + ENDPART;
      return parseString(buf);
    }
    catch (SAXParseException e) {
      try {
        buf = replaceInvalidCharacters(buf);
        buf = escapeNestedCData(buf);
        return parseString(buf);
      } catch (SAXParseException e1) {
        throw new RuntimeException(e);
      }
    }
  }

  private Document parseString(String xmlString) throws SAXParseException {
    try {
      return docBuilder.parse(new InputSource(new StringReader(xmlString)));
    } catch (SAXParseException e) {
      throw e;
    } catch (SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String replaceInvalidCharacters(String buf) {
    return INVALID_XML_CHARS_PATTERN.matcher(buf).replaceAll(REPLACEMENT_CHARACTER);
  }

  /**
   * log4j writes messages between CDATA markers. If the message itself also contains a CData marker (for example in a SOAP Fault) it gets
   * nested. This is fixed in log4j itself (see bug 9514 and 37560), but we need to deal with logfiles created with older versions.
   */
  private String escapeNestedCData(String log4jEvents) {
    String escapedLog4jEvents = log4jEvents;
    
    for (String tag : new String[] {"log4j:message", "log4j:NDC", "log4j:MDC", "log4j:throwable"}) {
      String startTag = "<" + tag + "><![CDATA[";
      int startIndex = escapedLog4jEvents.indexOf(startTag, 0);
      
      while(startIndex > -1) {
        startIndex = startIndex + startTag.length();
        int endIndex = escapedLog4jEvents.indexOf("]]></" + tag + ">", startIndex);
        
        String before = escapedLog4jEvents.substring(0, startIndex);
        String content = escapedLog4jEvents.substring(startIndex, endIndex).replace("]]>", "]]>]]&gt;<![CDATA[");
        String after = escapedLog4jEvents.substring(endIndex);
        escapedLog4jEvents = before + content + after;
        
        startIndex = escapedLog4jEvents.indexOf(startTag, startIndex);
      }
    }
    return escapedLog4jEvents;
  }
  
  /**
   * Decodes a File into a Vector of LoggingEvents.
   *
   * @param url the url of a file containing events to decode
   * @return Vector of LoggingEvents
   * @throws IOException if IO error during processing.
   */
  public Vector decode(final URL url) throws IOException {
    LineNumberReader reader;
    if (owner != null) {
      reader = new LineNumberReader(new InputStreamReader(new ProgressMonitorInputStream(owner, "Loading " + url, url.openStream())));
    } else {
      reader = new LineNumberReader(new InputStreamReader(url.openStream()));
    }

    Vector v = new Vector();

    String line;
    Vector events;
    try {
      while ((line = reader.readLine()) != null) {
        StringBuilder buffer = new StringBuilder(line);
        for (int i = 0; i < 1000; i++) {
          buffer.append(reader.readLine()).append("\n");
        }
        events = decodeEvents(buffer.toString());
        if (events != null) {
          v.addAll(events);
        }
      }
    } finally {
      partialEvent = null;
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (Exception e) {
        LOGGER.warn("Can't close reader");
      }
    }
    return v;
  }

  /**
   * Decodes a String representing a number of events into a Vector of LoggingEvents.
   *
   * @param document to decode events from
   * @return Vector of LoggingEvents
   */
  public Vector<LoggingEvent> decodeEvents(final String document) {
    if (document != null) {
      if (document.trim().equals("")) {
        return null;
      }
      String newDoc = null;
      String newPartialEvent;
      // separate the string into the last portion ending with
      // </log4j:event> (which will be processed) and the
      // partial event which will be combined and
      // processed in the next section

      // if the document does not contain a record end,
      // append it to the partial event string
      if (document.lastIndexOf(RECORD_END) == -1) {
        partialEvent = partialEvent + document;
        return null;
      }

      if (document.lastIndexOf(RECORD_END) + RECORD_END.length() < document.length()) {
        newDoc = document.substring(0, document.lastIndexOf(RECORD_END) + RECORD_END.length());
        newPartialEvent = document.substring(document.lastIndexOf(RECORD_END) + RECORD_END.length());
      } else {
        newDoc = document;
        newPartialEvent = "";
      }
      if (partialEvent != null) {
        newDoc = partialEvent + newDoc;
      }
      partialEvent = newPartialEvent;
      Document doc = parse(newDoc);
      if (doc == null) {
        return null;
      }
      return decodeEvents(doc);
    }
    return null;
  }

  /**
   * Converts the string data into an XML Document, and then soaks out the relevant bits to form a new LoggingEvent instance which can be used by any Log4j
   * element locally.
   *
   * @param data XML fragment
   * @return a single LoggingEvent
   */
  public LoggingEvent decode(final String data) {
    Document document = parse(data);

    if (document == null) {
      return null;
    }

    Vector<LoggingEvent> events = decodeEvents(document);

    if (events.size() > 0) {
      return events.firstElement();
    }

    return null;
  }

  /**
   * Given a Document, converts the XML into a Vector of LoggingEvents.
   *
   * @param document XML document
   * @return Vector of LoggingEvents
   */
  private Vector<LoggingEvent> decodeEvents(final Document document) {
    Vector<LoggingEvent> events = new Vector<>();

    String logger;
    long timeStamp;
    String level;
    String threadName;
    Object message = null;
    String ndc = null;
    String[] exception = null;
    String className = null;
    String methodName = null;
    String fileName = null;
    String lineNumber = null;
    Hashtable properties = null;

    NodeList nl = document.getElementsByTagName("log4j:eventSet");
    Node eventSet = nl.item(0);

    NodeList eventList = eventSet.getChildNodes();

    for (int eventIndex = 0; eventIndex < eventList.getLength(); eventIndex++) {
      Node eventNode = eventList.item(eventIndex);
      // ignore carriage returns in xml
      if (eventNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      logger = eventNode.getAttributes().getNamedItem("logger").getNodeValue();
      timeStamp = Long.parseLong(eventNode.getAttributes().getNamedItem("timestamp").getNodeValue());
      level = eventNode.getAttributes().getNamedItem("level").getNodeValue();
      threadName = eventNode.getAttributes().getNamedItem("thread").getNodeValue();

      NodeList list = eventNode.getChildNodes();
      int listLength = list.getLength();

      for (int y = 0; y < listLength; y++) {
        String tagName = list.item(y).getNodeName();

        if (tagName.equalsIgnoreCase("log4j:message")) {
          message = getCData(list.item(y));
        }

        if (tagName.equalsIgnoreCase("log4j:NDC")) {
          ndc = getCData(list.item(y));
        }
        // still support receiving of MDC and convert to properties
        if (tagName.equalsIgnoreCase("log4j:MDC")) {
          properties = new Hashtable();
          if (((Element) list.item(y)).getElementsByTagName("log4j:data").getLength() > 0) {
            decodePropertyChildNodes(properties, list.item(y));
          } else {
            decodePropertyString(properties, getCData(list.item(y)));
          }
        }

        if (tagName.equalsIgnoreCase("log4j:throwable")) {
          exception = new String[]{getCData(list.item(y))};
        }

        if (tagName.equalsIgnoreCase("log4j:locationinfo")) {
          className = list.item(y).getAttributes().getNamedItem("class").getNodeValue();
          methodName = list.item(y).getAttributes().getNamedItem("method").getNodeValue();
          fileName = list.item(y).getAttributes().getNamedItem("file").getNodeValue();
          lineNumber = list.item(y).getAttributes().getNamedItem("line").getNodeValue();
        }

        if (tagName.equalsIgnoreCase("log4j:properties")) {
          if (properties == null) {
            properties = new Hashtable();
          }
          decodePropertyChildNodes(properties, list.item(y));
        }

        /**
         * We add all the additional properties to the properties hashtable. Don't override properties that already exist
         */
        if (additionalProperties.size() > 0) {
          if (properties == null) {
            properties = new Hashtable(additionalProperties);
          } else {
            Iterator i = additionalProperties.entrySet().iterator();
            while (i.hasNext()) {
              Map.Entry e = (Map.Entry) i.next();
              if (!(properties.containsKey(e.getKey()))) {
                properties.put(e.getKey(), e.getValue());
              }
            }
          }
        }
      }
      Level levelImpl = Level.toLevel(level);

      LocationInfo info = LocationInfo.NA_LOCATION_INFO;
      if ((fileName != null) || (className != null) || (methodName != null) || (lineNumber != null)) {
        info = new LocationInfo(fileName, className, methodName, lineNumber);

      if (exception == null) {
        exception = new String[]{""};
      }

      LoggingEvent loggingEvent = new LoggingEvent(null, logger, timeStamp, levelImpl, message, threadName, new ThrowableInformation(exception), ndc, info,
        properties);
      // loggingEvent.setLogger(logger);
      // loggingEvent.setTimeStamp(timeStamp);
      // loggingEvent.setLevel(levelImpl);
      // loggingEvent.setThreadName(threadName);
      // loggingEvent.setMessage(message);
      // loggingEvent.setNDC(ndc);
      // loggingEvent.setThrowableInformation(new ThrowableInformation(exception));
      // loggingEvent.setLocationInformation(info);
      // loggingEvent.setProperties(properties);

      events.add(loggingEvent);

      message = null;
      ndc = null;
      exception = null;
      className = null;
      methodName = null;
      fileName = null;
      lineNumber = null;
      properties = null;
    }

    return events;
  }

  private void decodePropertyChildNodes(Hashtable<String, String> properties, Node mdcNode) {
    NodeList propertyList = mdcNode.getChildNodes();
    int propertyLength = propertyList.getLength();

    for (int i = 0; i < propertyLength; i++) {
      String propertyTag = propertyList.item(i).getNodeName();

      if (propertyTag.equalsIgnoreCase("log4j:data")) {
        Node property = propertyList.item(i);
        String name = property.getAttributes().getNamedItem("name").getNodeValue();
        String value = property.getAttributes().getNamedItem("value").getNodeValue();
        properties.put(name, value);
      }
    }
  }

  private void decodePropertyString(Map<String, String> properties, String mdcString) {
    Matcher matcher = KEY_OR_EOL_PATTERN.matcher(mdcString);
    int startOfKey = 0;
    while (matcher.find()) {
      int endOfValue = matcher.start();
      int seperator = mdcString.indexOf('=', startOfKey);
      if (seperator != -1) {
        String key = mdcString.substring(startOfKey, seperator);
        String value = mdcString.substring(seperator + 1, endOfValue);
        properties.put(key, value);
      } else {
        // handle corrupted key-value pair
        String key = mdcString.substring(startOfKey, endOfValue);
        properties.put(key, "");
      }
      startOfKey = endOfValue + 1; // skip the space
    }
  }

  /**
   * Get contents of CDATASection.
   *
   * @param n CDATASection
   * @return text content of all text or CDATA children of node.
   */
  private String getCData(final Node n) {
    StringBuffer buf = new StringBuffer();
    NodeList nl = n.getChildNodes();

    for (int x = 0; x < nl.getLength(); x++) {
      Node innerNode = nl.item(x);

      if ((innerNode.getNodeType() == Node.TEXT_NODE) || (innerNode.getNodeType() == Node.CDATA_SECTION_NODE)) {
        buf.append(innerNode.getNodeValue());
      }
    }

    return buf.toString();
  }
}
