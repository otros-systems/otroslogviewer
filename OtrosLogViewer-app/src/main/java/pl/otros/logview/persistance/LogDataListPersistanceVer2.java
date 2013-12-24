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
package pl.otros.logview.persistance;

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.LogData;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogDataListPersistanceVer2 implements LogDataListPersistance {

  private static final Logger LOGGER = Logger.getLogger(LogDataListPersistanceVer2.class.getName());

  public static final String HEADER_ID = "ID";
  public static final String HEADER_TIMESTAMP = "TIMESTAMP";
  public static final String HEADER_MESSAGE = "MESSAGE";
  public static final String HEADER_CLASS = "CLASS";
  public static final String HEADER_METHOD = "METHOD";
  public static final String HEADER_LEVEL = "LEVEL";
  public static final String HEADER_LOGGER = "LOGGER";
  public static final String HEADER_THREAD = "THREAD";
  public static final String HEADER_MDC = "MDC";
  public static final String HEADER_NDC = "NDC";
  public static final String HEADER_FILE = "FILE";
  public static final String HEADER_LINE = "LINE";
  public static final String HEADER_LOG_SOURCE = "LOG_SOURCE";
  public static final String HEADER_NOTE = "NOTE";
  public static final String HEADER_MARKED = "MARKED";
  public static final String HEADER_MARKED_COLOR = "MARKED_COLOR";

  public static final String FIELD_SEPERATOR = "|";
  public static final String FIELD_SEPERATOR_TO_SPLIT = "\\|";

  private static final String EMPTY_STRING = "";

  /*
   * (non-Javadoc)
   * 
   * @see pl.otros.logview.persistance.LogDataListPersistance#saveLogsList(java.io.OutputStream, java.util.List)
   */
  public void saveLogsList(OutputStream out, List<LogData> list) throws IOException {
    BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

    ArrayList<String> saveMapOrder = getSaveMapOrder();
    for (String string : saveMapOrder) {
      bout.write(string);
      bout.write(FIELD_SEPERATOR);
    }
    bout.write("\n");
    for (LogData logData : list) {
      Map<String, String> map = toMap(logData);
      for (String key : saveMapOrder) {
        String s = map.get(key);
        if (s == null) {
          s = "";
        }
        bout.write(escpageString(s));
        bout.write(FIELD_SEPERATOR);
      }
      bout.write("\n");
    }
    bout.flush();

  }

  private Map<String, String> toMap(LogData logData) {
    Map<String, String> m = new HashMap<String, String>();
    m.put(HEADER_ID, Integer.toString(logData.getId()));
    m.put(HEADER_CLASS, logData.getClazz());
    m.put(HEADER_LEVEL, logData.getLevel().toString());
    m.put(HEADER_LOGGER, logData.getLoggerName());
    m.put(HEADER_MESSAGE, logData.getMessage());
    m.put(HEADER_METHOD, logData.getMethod());
    m.put(HEADER_THREAD, logData.getThread());
    m.put(HEADER_TIMESTAMP, Long.toString(logData.getDate().getTime()));
    m.put(HEADER_FILE, logData.getFile());
    m.put(HEADER_LINE, logData.getLine());
    m.put(HEADER_LOG_SOURCE, logData.getLogSource());
    m.put(HEADER_NDC, logData.getNDC());
    String note = logData.getNote() != null ? logData.getNote().getNote() : null;
    m.put(HEADER_NOTE, note);
    m.put(HEADER_MARKED, Boolean.valueOf(logData.isMarked()).toString());
    String markerColors = logData.getMarkerColors() != null ? logData.getMarkerColors().toString() : "";
    m.put(HEADER_MARKED_COLOR, markerColors);

    String mdc = EMPTY_STRING;
    Map<String, String> properties = logData.getProperties();
    if (properties != null && properties.size() > 0) {
      Properties p = new Properties();
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      for (String key : properties.keySet()) {
        p.put(key, properties.get(key));
      }
      try {
        p.store(bout, null);
        mdc = bout.toString();
        mdc = mdc.substring(mdc.indexOf("\n") + 1);
      } catch (IOException e) {
        LOGGER.severe(String.format("Can't save LogData (id=%d) properties: %s", logData.getId(), e.getMessage()));
      }

    }

    m.put(HEADER_MDC, mdc);
    return m;
  }

  private ArrayList<String> getSaveMapOrder() {
    ArrayList<String> m = new ArrayList<String>();
    m.add(HEADER_ID);
    m.add(HEADER_TIMESTAMP);
    m.add(HEADER_MESSAGE);
    m.add(HEADER_CLASS);
    m.add(HEADER_METHOD);
    m.add(HEADER_LEVEL);
    m.add(HEADER_LOGGER);
    m.add(HEADER_THREAD);
    m.add(HEADER_MDC);
    m.add(HEADER_NDC);
    m.add(HEADER_FILE);
    m.add(HEADER_LINE);
    m.add(HEADER_LOG_SOURCE);
    m.add(HEADER_NOTE);
    m.add(HEADER_MARKED);
    m.add(HEADER_MARKED_COLOR);

    return m;
  }

  /*
   * (non-Javadoc)
   * 
   * @see pl.otros.logview.persistance.LogDataListPersistance#loadLogsList(java.io.InputStream)
   */
  public List<LogData> loadLogsList(InputStream in) throws IOException {
    BufferedReader bin = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    String line = bin.readLine();
    String[] split = line.split(FIELD_SEPERATOR_TO_SPLIT);
    HashMap<String, Integer> fieldMapping = new HashMap<String, Integer>();
    for (int i = 0; i < split.length; i++) {
      String string = split[i];
      fieldMapping.put(string, Integer.valueOf(i));
    }

    ArrayList<LogData> list = new ArrayList<LogData>();
    while ((line = bin.readLine()) != null) {
      String[] params = line.split(FIELD_SEPERATOR_TO_SPLIT);
      // Splitting 1|1317761093265|My Message1|||INFO||| will give only 6 params!
      if (params.length < split.length) {
        String[] newParams = new String[split.length];
        System.arraycopy(params, 0, newParams, 0, params.length);
        for (int i = params.length; i < newParams.length; i++) {
          newParams[i] = "";
        }
        params = newParams;
      }
      LogData parseLogData = parseLogData(params, fieldMapping);
      list.add(parseLogData);
    }

    return list;
  }

  protected LogData parseLogData(String[] line, Map<String, Integer> fieldMapping) {
    for (int i = 0; i < line.length; i++) {
      line[i] = unescapgeString(line[i]);
    }
    LogData ld = new LogData();
    ld.setId(Integer.parseInt(line[fieldMapping.get(HEADER_ID)]));
    ld.setClazz(line[fieldMapping.get(HEADER_CLASS)]);
    ld.setDate(new Date(Long.parseLong(line[fieldMapping.get(HEADER_TIMESTAMP)])));
    ld.setLevel(Level.parse(line[fieldMapping.get(HEADER_LEVEL)]));
    ld.setLoggerName(line[fieldMapping.get(HEADER_LOGGER)]);
    ld.setMessage(line[fieldMapping.get(HEADER_MESSAGE)]);
    ld.setMethod(line[fieldMapping.get(HEADER_METHOD)]);
    ld.setThread(line[fieldMapping.get(HEADER_THREAD)]);

    // Checking if field is set for backward compatibility
    if (fieldMapping.containsKey(HEADER_MDC)) {
      String p = line[fieldMapping.get(HEADER_MDC)];
      Properties pr = new Properties();
      try {
        pr.load(new ByteArrayInputStream(p.getBytes()));
        Map<String, String> m = new HashMap<String, String>();
        for (Object key : pr.keySet()) {
          m.put((String) key, (String) pr.get(key));
        }
        if (m.size() > 0) {
          ld.setProperties(m);
        }
      } catch (IOException e) {
        LOGGER.severe(String.format("Can't load LogData (id=%d) properties: %s", ld.getId(), e.getMessage()));
      }
    }

    if (fieldMapping.containsKey(HEADER_NDC)) {
      ld.setNDC(line[fieldMapping.get(HEADER_NDC)]);
    }

    if (fieldMapping.containsKey(HEADER_FILE)) {
      ld.setFile(line[fieldMapping.get(HEADER_FILE)]);
    }

    if (fieldMapping.containsKey(HEADER_LINE)) {
      ld.setLine(line[fieldMapping.get(HEADER_LINE)]);
    }

    if (fieldMapping.containsKey(HEADER_LOG_SOURCE)) {
      ld.setLogSource(line[fieldMapping.get(HEADER_LOG_SOURCE)]);
    }

    if (fieldMapping.containsKey(HEADER_NOTE) && StringUtils.isNotBlank(line[fieldMapping.get(HEADER_NOTE)])) {
      ld.setNote(new Note(line[fieldMapping.get(HEADER_NOTE)]));
    }

    if (fieldMapping.containsKey(HEADER_MARKED)) {
      ld.setMarked(Boolean.parseBoolean(line[fieldMapping.get(HEADER_MARKED)]));
    }

    if (fieldMapping.containsKey(HEADER_MARKED_COLOR) && StringUtils.isNotBlank(line[fieldMapping.get(HEADER_MARKED_COLOR)])) {
      ld.setMarkerColors(MarkerColors.valueOf(line[fieldMapping.get(HEADER_MARKED_COLOR)]));
    }

    return ld;
  }

  protected String escpageString(String s) {
    s = s.replace("\\", "\\S"); // "\" -> "\S"
    s = s.replace("|", "\\P"); // "|" -> "\P"
    s = s.replace("\n", "\\n");
    s = s.replace("\r", "\\r");
    return s;
  }

  protected String unescapgeString(String s) {
    s = s.replace("\\n", "\n");
    s = s.replace("\\r", "\r");
    s = s.replace("\\P", "|");
    s = s.replace("\\S", "\\");
    return s;
  }

}
