package pl.otros.logview.parser.json;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.util.Validator;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.parser.I18nLevelParser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class JsonExtractor {

  public static final String VALUES_SEPARATOR = ".";
  private String propertyLevel;
  private List<String> propertyKeysToMdc;
  private String propertyMessage;
  private String propertyDate;
  private String propertyDateFormat;
  private String propertyThread;
  private String propertyClass;
  private String propertyMethod;
  private String propertyLine;
  private String propertyNdc;
  private String propertyNote;
  private String propertyMarkerColor;

  private String propertyLogger;
  private String propertyFile;
  private I18nLevelParser i18nLevelParser;


  public void init(Properties properties) {
    propertyLevel = properties.getProperty("level", "level");
    propertyMessage = properties.getProperty("message", "message");
    propertyDate = properties.getProperty("date", "date");
    propertyDateFormat = properties.getProperty("dateFormat", "timestamp");
    propertyThread = properties.getProperty("thread", "thread");
    propertyFile = properties.getProperty("file", "file");
    propertyClass = properties.getProperty("class", "class");
    propertyMethod = properties.getProperty("method", "method");
    propertyLine = properties.getProperty("line", "line");
    propertyLogger = properties.getProperty("logger", "logger");
    propertyNdc = properties.getProperty("ndc", "ndc");
    propertyNote = properties.getProperty("note", "note");
    propertyMarkerColor = properties.getProperty("markerColor", "markerColor");
    propertyKeysToMdc = Splitter.on(",").trimResults().splitToList(properties.getProperty("mdcKeys", ""));

    i18nLevelParser = new I18nLevelParser(Locale.ENGLISH);
  }

  public DateFormat createDateFormatter() {
    if (!propertyDateFormat.equalsIgnoreCase("timestamp")) {
      return new SimpleDateFormat(propertyDateFormat);
    } else {
      return new TimestampDateFormat();
    }
  }

  /**
   * Tries to parse log event in json form using selected date format
   *
   * @param s          log fragment
   * @param dateFormat instance of DateFormat
   * @return Optional of LogData if log event can be extracted, empty if not.
   */
  Optional<LogData> parseJsonLog(String s, DateFormat dateFormat) {
    try {
      Validator.validate(s);
      final JSONObject jsonObject = new JSONObject(s);
      final Map<String, String> map = toMap(jsonObject);
      return mapToLogData(map, dateFormat);
    } catch (JSONException e) {
      return Optional.empty();

    }
  }

  /**
   * Tries convert json object in map form to log data
   *
   * @param map        json object in map form, names as xpath separated by dots are keys.
   *                   <code>
   *                   {"a": "value1"
   *                   "b": {
   *                   "c": "value2"
   *                   }
   *                   }
   *                   </code>
   *                   will be represented by map
   *                   a   -> value1,
   *                   b.c -> value2
   * @param dateParser date format
   * @return Optional of LogData in case of success or Optional.empty in case of error
   */
  Optional<LogData> mapToLogData(Map<String, String> map, DateFormat dateParser) {
    LogDataBuilder builder = new LogDataBuilder();

    //TODO parsing level based on custom mapping
    builder.withLevel(i18nLevelParser.parse(map.get(propertyLevel).trim()));
    final String dateString = map.get(propertyDate);

    try {
      builder = builder
        .withDate(dateParser.parse(dateString))
        .withMessage(map.getOrDefault(propertyMessage, ""))
        .withThread(map.getOrDefault(propertyThread, ""))
        .withLoggerName(map.getOrDefault(propertyLogger, ""))
        .withClass(map.getOrDefault(propertyClass, ""))
        .withMethod(map.getOrDefault(propertyMethod, ""))
        .withLineNumber(map.getOrDefault(propertyLine, ""))
        .withFile(map.getOrDefault(propertyFile, ""))
        .withNote(new Note(map.getOrDefault(propertyNote, "")))
        .withNdc(map.getOrDefault(propertyNdc,""))
      ;
      final String color = map.getOrDefault(propertyMarkerColor, "");
      if (color != null) {
        builder = builder
          .withMarkerColors(MarkerColors.fromString(color))
          .withMarked(true);

      }
      //build mdc
      final Map<String, String> mdc = extractMdc(map, this.propertyKeysToMdc);
      builder = builder.withProperties(mdc);
      return Optional.of(builder.build());
    } catch (ParseException e) {
      return Optional.empty();
    }
  }

  /**
   * Get list of fields and populate values into map (if value for key is not empty
   *
   * @param map  map to fill
   * @param keys list of xpaths in json files
   * @return map populated by additional xpath values
   */
  static Map<String, String> extractMdc(Map<String, String> map, List<String> keys) {
    return keys.stream()
      .filter(key -> isNotBlank(map.getOrDefault(key, ""))
      )
      .collect(Collectors.toMap(key -> key, map::get));
  }

  /**
   * Convert Json object to map representation. Nested object are represented by keys like xpath (dot separated)
   *
   * @param j Json object t convert
   * @return map representation of json object
   * @throws JSONException
   */
  public static Map<String, String> toMap(JSONObject j) throws JSONException {
    return toMap(j, new HashMap<>(), StringUtils.EMPTY);
  }

  private static Map<String, String> toMap(JSONObject j, Map<String, String> map, String prefix) throws JSONException {
    final Iterator<String> keys = j.keys();
    while (keys.hasNext()) {
      final String key = keys.next();
      final Object o = j.get(key);
      if (o instanceof JSONObject) {
        JSONObject jso = (JSONObject) o;
        toMap(jso, map, prefix + key + VALUES_SEPARATOR);
      } else {
        map.put(prefix + key, j.getString(key));
      }
    }
    return map;
  }
}
