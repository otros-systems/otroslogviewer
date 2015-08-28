package pl.otros.logview.parser.json;

import com.google.common.base.Splitter;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.util.Validator;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.parser.log4j.Log4jUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class JsonExtractor {

  private String propertyLevel;
  private List<String> propertyKeysToMdc;
  private String propertyMessage;
  private String propertyDate;
  private String propertyDateFormat;
  private String propertyThread;
  private String propertyClass;
  private String propertyMethod;
  private String propertyLine;

  private String propertyLogger;
  private String propertyFile;


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
    propertyKeysToMdc = Splitter.on(",").trimResults().splitToList(properties.getProperty("mdcKeys", ""));
  }

  public DateFormat createDateFormatter() {
    if (!propertyDateFormat.equalsIgnoreCase("timestamp")) {
      return new SimpleDateFormat(propertyDateFormat);
    } else {
      return new TimestampDateFormat();
    }
  }

  Optional<LogData> parseJsonLog(String s, DateFormat dateFormat) {
    try {
      Validator.validate(s);
      final JSONObject jsonObject = new JSONObject(s);
      final Map<String, String> map = toMap(jsonObject, new HashMap<>(), "");
      return mapToLogData(map, dateFormat);
    } catch (JSONException e) {
      return Optional.empty();

    }
  }

  Optional<LogData> mapToLogData(Map<String, String> map, DateFormat dateParser) {
    LogDataBuilder builder = new LogDataBuilder();

    //TODO parsing levels
    builder.withLevel(Log4jUtil.parseLevel(map.get(propertyLevel).trim()));
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
      ;
      //build mdc
      final Map<String, String> mdc = extractMdc(map, this.propertyKeysToMdc);
      builder = builder.withProperties(mdc);
      return Optional.of(builder.build());
    } catch (ParseException e) {
      return Optional.empty();
    }
  }

  static Map<String, String> extractMdc(Map<String, String> map, List<String> keys) {
    return keys.stream()
      .filter(key -> {
          final String orDefault = map.getOrDefault(key, "");
          return orDefault.trim().length() != 0;
        }
      )
      .collect(Collectors.toMap(key -> key, map::get));
  }

  public static Map<String, String> toMap(JSONObject j) throws JSONException {
    return toMap(j, new HashMap<>(), "");
  }

  public static Map<String, String> toMap(JSONObject j, Map<String, String> map, String prefix) throws JSONException {
    final Iterator<String> keys = j.keys();
    while (keys.hasNext()) {
      final String key = keys.next();
      final Object o = j.get(key);
      if (o instanceof JSONObject) {
        JSONObject jso = (JSONObject) o;
        toMap(jso, map, prefix + key + ".");
      } else {
        map.put(prefix + key, j.getString(key));
      }
    }
    return map;
  }
}
