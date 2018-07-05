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
package pl.otros.logview.importer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.importer.log4jxml.SAXErrorHandler;
import pl.otros.logview.importer.log4jxml.UtilLoggingEntityResolver;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;

public class UtilLoggingXmlLogImporter extends AbstractPluginableElement implements LogImporter {

  private static final String DOC_BUILDER = "DOC_BUILDER";
  private static final String PARTIAL_EVENT = "PARTIAL_EVENT";
  private static final Logger LOGGER = LoggerFactory.getLogger(UtilLoggingXmlLogImporter.class.getName());
  private static final String NAME = "Improved XMLFormatter";
  private Icon icon;
  private static final String ICON_PATH = "img/java.png";

  public UtilLoggingXmlLogImporter() {
    super(NAME, NAME);
  }

  // NOTE: xml section is only handed on first delivery of events
  // on this first delivery of events, there is no end tag for the log element
  /**
   * Document prolog.
   */
  private static final String BEGIN_PART = "<log>";
  /**
   * Document close.
   */
  private static final String END_PART = "</log>";
  /**
   * Document builder.
   */
  // private DocumentBuilder docBuilder;

  /**
   * Record end.
   */
  private static final String RECORD_END = "</record>";

  private static final String ENCODING = "UTF-8";

  @Override
  public void init(Properties properties) throws InitializationException {
    try {
      icon = new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(ICON_PATH)));
      LOGGER.info("icon loaded");
    } catch (Exception e) {
      LOGGER.warn("Error loading icon: " + e.getMessage());
    }

  }

  @Override
  public void importLogs(InputStream in, LogDataCollector collector, ParsingContext parsingContext) {

    StringBuilder sb = new StringBuilder();
    try (LineNumberReader bin = new LineNumberReader(new InputStreamReader(in, ENCODING))) {
      String line;
      while ((line = bin.readLine()) != null) {
        sb.append(line).append("\n");
        if (bin.getLineNumber() % 30 == 0) {
          decodeEvents(sb.toString(), collector, parsingContext);
          sb.setLength(0);
        }
      }
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Cant load codepage " + e.getMessage());
    } catch (IOException ignore) {
      LOGGER.debug("Logs can't be imported due to IOException, most probably stream was closed");
      LOGGER.error("Exception:", ignore);
    } finally {
      decodeEvents(sb.toString(), collector, parsingContext);
      IOUtils.closeQuietly(in);
    }

  }

  /**
   * Converts the LoggingEvent data in XML string format into an actual XML Document class instance.
   *
   * @param data XML fragment
   * @return dom document
   */
  private Document parse(final String data, DocumentBuilder docBuilder) {
    if (docBuilder == null || data == null) {
      return null;
    }

    Document document = null;

    try {
      // we change the system ID to a valid URI so that Crimson won't
      // complain. Indeed, "log4j.dtd" alone is not a valid URI which
      // causes Crimson to barf. The Log4jEntityResolver only cares
      // about the "log4j.dtd" ending.

      /**
       * resetting the length of the StringBuffer is dangerous, particularly on some JDK 1.4 impls, there's a known Bug that causes a memory leak
       */
      StringBuilder buf = new StringBuilder(1024);

      if (!data.startsWith("<?xml")) {
        buf.append(BEGIN_PART);
      }

      buf.append(data);

      if (!data.endsWith(END_PART)) {
        buf.append(END_PART);
      }

      InputSource inputSource = new InputSource(new StringReader(buf.toString()));
      document = docBuilder.parse(inputSource);

    } catch (Exception e) {
      LOGGER.warn("Problem with creating document: " + e.getMessage());

    }

    return document;
  }

  /**
   * Decodes a String representing a number of events into a Vector of LoggingEvents.
   *
   * @param document to decode events from
   * @return Vector of LoggingEvents
   */
  public void decodeEvents(final String document, LogDataCollector collector, ParsingContext parsingContext) {

    if (document != null) {

      if (document.trim().equals("")) {
        return;
      }

      String newDoc;
      String newPartialEvent;
      // separate the string into the last portion ending with </record>
      // (which will be processed) and the partial event which
      // will be combined and processed in the next section

      String partialEvent = (String) parsingContext.getCustomConextProperties().get(PARTIAL_EVENT);
      // if the document does not contain a record end,
      // append it to the partial event string
      if (document.lastIndexOf(RECORD_END) == -1) {
        partialEvent = partialEvent + document;
        parsingContext.getCustomConextProperties().put(PARTIAL_EVENT, partialEvent);
        return;
      }

      if (document.lastIndexOf(RECORD_END) + RECORD_END.length() < document.length()) {
        newDoc = document.substring(0, document.lastIndexOf(RECORD_END) + RECORD_END.length());
        newPartialEvent = document.substring(document.lastIndexOf(RECORD_END) + RECORD_END.length());
        parsingContext.getCustomConextProperties().put(PARTIAL_EVENT, newPartialEvent);
      } else {
        newDoc = document;
      }
      if (partialEvent != null) {
        newDoc = partialEvent + newDoc;
      }

      Document doc = parse(newDoc, (DocumentBuilder) parsingContext.getCustomConextProperties().get(DOC_BUILDER));
      if (doc == null) {
        return;
      }
      decodeEvents(doc, collector, parsingContext);
    }
  }

  /**
   * Given a Document, converts the XML into a Vector of LoggingEvents.
   *
   * @param document XML document
   * @return Vector of LoggingEvents
   */
  private void decodeEvents(final Document document, LogDataCollector collector, ParsingContext parsingContext) {

    NodeList eventList = document.getElementsByTagName("record");

    for (int eventIndex = 0; eventIndex < eventList.getLength(); eventIndex++) {
      Node eventNode = eventList.item(eventIndex);

      Logger logger = null;
      long timeStamp = 0L;
      Level level = null;
      String threadName = null;
      Object message = null;
      String className = null;
      String methodName = null;
      String exceptionStackTrace = null;

      // format of date: 2003-05-04T11:04:52
      // ignore date or set as a property? using millis in constructor instead
      NodeList list = eventNode.getChildNodes();
      int listLength = list.getLength();

      if (listLength == 0) {
        continue;
      }

      for (int y = 0; y < listLength; y++) {
        Node logEventNode = list.item(y);
        String tagName = logEventNode.getNodeName();

        if (tagName.equalsIgnoreCase("logger")) {
          logger = LoggerFactory.getLogger(getCData(list.item(y)));
        } else if (tagName.equalsIgnoreCase("millis")) {
          timeStamp = Long.parseLong(getCData(list.item(y)));
        } else if (tagName.equalsIgnoreCase("level")) {
          level = Level.parse(getCData(list.item(y)));
        } else if (tagName.equalsIgnoreCase("thread")) {
          threadName = getCData(list.item(y));
        } else if (tagName.equalsIgnoreCase("message")) {
          message = getCData(list.item(y));
        } else if (tagName.equalsIgnoreCase("class")) {
          className = getCData(list.item(y));
        } else if (tagName.equalsIgnoreCase("method")) {
          methodName = getCData(list.item(y));
        } else if (tagName.equalsIgnoreCase("exception")) {
          exceptionStackTrace = getExceptionStackTrace(list.item(y));

        }


      }
      if (message != null && exceptionStackTrace != null) {
        message = message + "\n" + exceptionStackTrace;
      } else if (exceptionStackTrace != null) {
        message = exceptionStackTrace;
      }
      LogData logData = new LogData();
      logData.setLevel(level);
      logData.setClazz(className);
      logData.setId(parsingContext.getGeneratedIdAndIncrease());
      logData.setDate(new Date(timeStamp));
      logData.setLoggerName(logger.getName());
      logData.setMessage(StringUtils.defaultString(message != null ? message.toString() : ""));
      logData.setThread(threadName);
      logData.setMethod(methodName);
      logData.setLogSource(parsingContext.getLogSource());
      collector.add(logData);

    }
  }

  protected String getExceptionStackTrace(Node eventNode) {
    StringBuilder sb = new StringBuilder();
    NodeList childNodes = eventNode.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      String tagName = childNode.getNodeName();
      if (tagName.equalsIgnoreCase("message")) {
        sb.append(getCData(childNodes.item(i)));
      } else if (tagName.equalsIgnoreCase("frame")) {
        getStackTraceFrame(childNodes.item(i), sb);
      }
    }

    return sb.toString();

  }

  protected void getStackTraceFrame(Node item, StringBuilder sb) {
    NodeList childNodes = item.getChildNodes();

    String clazz = null;
    String method = null;
    String line = null;
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      String tagName = childNode.getNodeName();
      if (tagName.equalsIgnoreCase("class")) {
        clazz = getCData(childNode);
      } else if (tagName.equalsIgnoreCase("method")) {
        method = getCData(childNode);
      } else if (tagName.equalsIgnoreCase("line")) {
        line = getCData(childNode);
      }
    }

    //default string if clazz does not contain package
    String fileName = extractFileName(clazz);

    appendStackFrame(sb, clazz, method, line, fileName);

  }

  protected StringBuilder appendStackFrame(StringBuilder sb, String clazz, String method, String line, String fileName) {
    sb.append("\n\tat ");
    sb.append(clazz).append(".").append(method);
    if (fileName != null) {
      sb.append("(");
      sb.append(fileName).append(".java");
      if (line != null) {
        sb.append(":").append(line);
      }
      sb.append(")");
    }
    return sb;
  }

  protected String extractFileName(String clazz) {
    int clazzStart = StringUtils.lastIndexOf(clazz, '.') + 1;
    clazzStart = Math.max(0, clazzStart);
    int clazzEnd = StringUtils.indexOf(clazz, '$');
    if (clazzEnd < 0) {
      clazzEnd = clazz.length();
    }
    return StringUtils.substring(clazz, clazzStart, clazzEnd);
  }

  /**
   * Get contents of CDATASection.
   *
   * @param n CDATASection
   * @return text content of all text or CDATA children of node.
   */
  private String getCData(final Node n) {
    StringBuilder buf = new StringBuilder();
    NodeList nl = n.getChildNodes();

    for (int x = 0; x < nl.getLength(); x++) {
      Node innerNode = nl.item(x);

      if ((innerNode.getNodeType() == Node.TEXT_NODE) || (innerNode.getNodeType() == Node.CDATA_SECTION_NODE)) {
        buf.append(innerNode.getNodeValue());
      }
    }

    return buf.toString();
  }

  @Override
  public String getKeyStrokeAccelelator() {
    return "control l";
  }

  @Override
  public int getMnemonic() {
    return KeyEvent.VK_L;
  }

  @Override
  public Icon getIcon() {
    return icon;
  }

  @Override
  public TableColumns[] getTableColumnsToUse() {
    return TableColumns.JUL_COLUMNS;
  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);

    try {
      DocumentBuilder docBuilder = dbf.newDocumentBuilder();
      docBuilder.setErrorHandler(new SAXErrorHandler());
      docBuilder.setEntityResolver(new UtilLoggingEntityResolver());
      parsingContext.getCustomConextProperties().put(DOC_BUILDER, docBuilder);
      parsingContext.getCustomConextProperties().put(PARTIAL_EVENT, "");
    } catch (ParserConfigurationException pce) {
      System.err.println("Unable to get document builder");
    }

  }

  @Override
  public int getApiVersion() {
    return LOG_IMPORTER_VERSION_1;
  }

}
