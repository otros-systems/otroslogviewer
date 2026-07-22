package pl.otros.logview.parser.json;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataBuilder;
import pl.otros.logview.api.model.MarkerColors;
import pl.otros.logview.api.model.Note;
import pl.otros.logview.parser.I18nLevelParser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class JsonExtractor {

  private static final String VALUES_SEPARATOR = ".";
  public static final String LEVEL = "level";
  public static final String MESSAGE = "message";
  public static final String DATE = "date";
  public static final String DATE_FORMAT = "dateFormat";
  public static final String THREAD = "thread";
  public static final String FILE = "file";
  public static final String CLASS = "class";
  public static final String METHOD = "method";
  public static final String LINE = "line";
  public static final String LOGGER = "logger";
  public static final String NDC = "ndc";
  public static final String NOTE = "note";
  private static final String EXCEPTION = "exception";
  private static final String MARKER_COLOR = "markerColor";
  public static final String MDC_KEYS = "mdcKeys";
  public static final String[] KEYS = {
    LEVEL,
    MESSAGE,
    DATE,
    DATE_FORMAT,
    THREAD,
    FILE,
    CLASS,
    METHOD,
    LINE,
    LOGGER,
    NDC,
    NOTE,
    MARKER_COLOR,
    MDC_KEYS
  };
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
  private String propertyException;
  private String propertyMarkerColor;

  private String propertyLogger;
  private String propertyFile;
  private I18nLevelParser i18nLevelParser;


  public void init(Properties properties) {
    propertyLevel = properties.getProperty(LEVEL, "level");
    propertyMessage = properties.getProperty(MESSAGE, "message");
    propertyDate = properties.getProperty(DATE, "date");
    propertyDateFormat = properties.getProperty(DATE_FORMAT, "timestamp");
    propertyThread = properties.getProperty(THREAD, "thread");
    propertyFile = properties.getProperty(FILE, "file");
    propertyClass = properties.getProperty(CLASS, "class");
    propertyMethod = properties.getProperty(METHOD, "method");
    propertyLine = properties.getProperty(LINE, "line");
    propertyLogger = properties.getProperty(LOGGER, "logger");
    propertyNdc = properties.getProperty(NDC, "ndc");
    propertyNote = properties.getProperty(NOTE, "note");
    propertyException = properties.getProperty(EXCEPTION, "exception");
    propertyMarkerColor = properties.getProperty(MARKER_COLOR, "markerColor");
    propertyKeysToMdc = Splitter.on(",").trimResults().splitToList(properties.getProperty(MDC_KEYS, ""));

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
  protected Optional<LogData> parseJsonLog(String s, DateFormat dateFormat) {
    try {
      final JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
      final Map<String, String> map = toMap(jsonObject);
      return mapToLogData(map, dateFormat);
    } catch (JsonSyntaxException | IllegalStateException e) {
      return Optional.empty();
    }
  }

  public List<String> extractFieldValues(String json, String field) {
    final List<String> lines = Arrays.stream(json.split("\n")).map(String::trim).toList();
    StringBuilder buffer = new StringBuilder();
    List<String> result = new ArrayList<>();
    for (String line : lines) {
      buffer.append(line);
      final String bufferContent = buffer.toString();
      try {
        final JsonObject jsonObject = JsonParser.parseString(bufferContent).getAsJsonObject();
        buffer.setLength(0);
        Optional.ofNullable(toMap(jsonObject).get(field))
          .ifPresent(result::add);
      } catch (JsonSyntaxException | IllegalStateException ignore) {
        //Do nothing for better performance
      }
    }
    return result;
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
  protected Optional<LogData> mapToLogData(Map<String, String> map, DateFormat dateParser) {
    LogDataBuilder builder = new LogDataBuilder();

    //TODO parsing level based on custom mapping
    builder.withLevel(i18nLevelParser.parse(map.get(propertyLevel).trim()));
    final Optional<String> dateString = Optional.ofNullable(map.get(propertyDate));

    String message = map.getOrDefault(propertyMessage, "");
    if (map.containsKey(propertyException)) {
      message = message + "\n" + map.get(propertyException);
    }

    builder = builder
      .withDate(dateString.map(d -> parse(dateParser, d)).orElse(new Date()))
      .withMessage(message)
      .withThread(map.getOrDefault(propertyThread, ""))
      .withLoggerName(map.getOrDefault(propertyLogger, ""))
      .withClass(map.getOrDefault(propertyClass, ""))
      .withMethod(map.getOrDefault(propertyMethod, ""))
      .withLineNumber(map.getOrDefault(propertyLine, ""))
      .withFile(map.getOrDefault(propertyFile, ""))
      .withNote(new Note(map.getOrDefault(propertyNote, "")))
      .withNdc(map.getOrDefault(propertyNdc, ""))
    ;
    final String color = map.getOrDefault(propertyMarkerColor, "");
    if (isNotBlank(color)) {
      builder = builder
        .withMarkerColors(MarkerColors.fromString(color))
        .withMarked(true);
    }
    //build mdc
    final Map<String, String> mdc = extractMdc(map, this.propertyKeysToMdc);
    builder = builder.withProperties(mdc);
    return Optional.of(builder.build());
  }

  private Date parse(DateFormat dateFormat, String date) {
    try {
      return dateFormat.parse(date);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Get list of fields and populate values into map (if value for key is not empty
   *
   * @param map  map to fill
   * @param keys list of xpaths in json files
   * @return map populated by additional xpath values
   */
  private static Map<String, String> extractMdc(Map<String, String> map, List<String> keys) {
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
   */
  public static Map<String, String> toMap(JsonObject j) {
    return toMap(j, new HashMap<>(), StringUtils.EMPTY);
  }

  private static Map<String, String> toMap(JsonObject j, Map<String, String> map, String prefix) {
    for (Map.Entry<String, JsonElement> entry : j.entrySet()) {
      final String key = entry.getKey();
      final JsonElement o = entry.getValue();
      if (o.isJsonObject()) {
        JsonObject jso = o.getAsJsonObject();
        toMap(jso, map, prefix + key + VALUES_SEPARATOR);
      } else if (o.isJsonPrimitive()) {
        map.put(prefix + key, o.getAsString());
      } else if (o.isJsonNull()) {
        map.put(prefix + key, "");
      }
    }
    return map;
  }

}
