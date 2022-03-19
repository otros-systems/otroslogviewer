package pl.otros.logview.parser.json.log4j2;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.util.Validator;
import pl.otros.logview.api.TableColumns;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataBuilder;
import pl.otros.logview.api.parser.LogParser;
import pl.otros.logview.api.parser.ParserDescription;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.parser.json.JsonExtractor;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;

import static pl.otros.logview.api.TableColumns.*;

//import pl.otros.logview.parser.json.log4j2.ExtendedStackTrace;

/**
 * Parser of log in json format. It can skip if between json object are some junk (from standard output). From properties
 * are taken mappings from Json ~xpath to LogData fields. For example if Json field logMessage contains information about
 * message, it should be written as message=logMessage
 */
public class Log4j2JsonLogParser extends AbstractPluginableElement implements LogParser {

  private final ParserDescription parserDescription;
  private final JsonExtractor jsonExtractor;
  private static final Map<String, Level> levelMap = new ImmutableMap.Builder<String, Level>()
    .put("TRACE", Level.FINEST)
    .put("DEBUG", Level.FINE)
    .put("INFO", Level.INFO)
    .put("WARN", Level.WARNING)
    .put("ERROR", Level.SEVERE)
    .put("FATAL", Level.SEVERE)
    .build();


  public Log4j2JsonLogParser() {
    super("Log4j2 Json parser", "Log4j2 Json log parser");
    parserDescription = new ParserDescription();
    parserDescription.setDisplayName(name);
    parserDescription.setDescription(description);
    jsonExtractor = new JsonExtractor();
  }

  @Override
  public void init(Properties properties) {
    jsonExtractor.init(properties);
  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
    parsingContext.setDateFormat(jsonExtractor.createDateFormatter());

  }

  @Override
  public LogData parse(String line, ParsingContext parsingContext) {

    final StringBuilder unmatchedLog = parsingContext.getUnmatchedLog();
    if (unmatchedLog.length() == 0 && !(line.matches("[,\\s\u0000]*\\{.*"))) {
      return null;
    }

    if (unmatchedLog.length() == 0 && (line.matches("[,\u0000].*"))) {
      unmatchedLog.append(line.substring(1)).append("\n");
    } else {
      unmatchedLog.append(line).append("\n");
    }

    String s = unmatchedLog.toString();
    try {
      Validator.validate(s);
      final Gson gson = new GsonBuilder()

        .registerTypeAdapter(new TypeToken<Thrown>() {
        }.getType(), new ThrownDeserializer())
        .create();

      final Log4j2JsonEvent log4j2JsonEvent = gson.fromJson(s, Log4j2JsonEvent.class);

      unmatchedLog.setLength(0);

      final Optional<Source> source = Optional.ofNullable(log4j2JsonEvent.getSource());

      final Optional<String> exception = Optional
        .ofNullable(log4j2JsonEvent.getThrown())
        .map(Thrown::getStacktrace);

      final long timestamp = Optional.ofNullable(log4j2JsonEvent.getInstant())
        .map(Log4j2JsonEvent.Instant::timestamp)
        .orElse(log4j2JsonEvent.getTimeMillis());

      return new LogDataBuilder()
        .withLevel(levelMap.get(log4j2JsonEvent.getLevel()))
        .withDate(new Date(timestamp))
        .withLoggerName(log4j2JsonEvent.getLoggerName())
        .withClass(log4j2JsonEvent.getLoggerName())
        .withMessage(log4j2JsonEvent.getMessage() + exception.map(e -> "\n" + e).orElse(""))
        .withThread(log4j2JsonEvent.getThread())
        .withMethod(source.map(Source::getMethod).orElse(""))
        .withFile(source.map(Source::getFile).orElse(""))
        .withLineNumber(source.map(s1 -> s1.getLine().toString()).orElse(""))
        .withProperties(log4j2JsonEvent.getContextMap())
        .build();

    } catch (JSONException ignored) {
    }

    return null;
  }


  @Override
  public TableColumns[] getTableColumnsToUse() {
    return new TableColumns[]{ID, LEVEL, TIME, MESSAGE, THREAD, LOGGER_NAME, CLASS, METHOD, LINE, FILE, NOTE, NDC, MARK, PROPERTIES};
  }

  @Override
  public ParserDescription getParserDescription() {
    return parserDescription;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public int getApiVersion() {
    return LOG_PARSER_VERSION_1;
  }


  @Override
  public String getName() {
    return parserDescription.getDisplayName();
  }

  @Override
  public String getDescription() {
    return parserDescription.getDescription();
  }


  private class ThrownDeserializer implements JsonDeserializer<Thrown> {
    @Override
    public Thrown deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      final JsonObject thrownObject = json.getAsJsonObject();
      final String message = thrownObject.get("message").getAsString();
      final String name = thrownObject.get("name").getAsString();
      final JsonElement extendedStackTrace = thrownObject.get("extendedStackTrace");
      if (extendedStackTrace.isJsonPrimitive()) {
        String stacktrace = extendedStackTrace.getAsString().trim();
        return new Thrown(message, name, stacktrace);
      } else if (extendedStackTrace.isJsonArray()) {
        final JsonArray array = extendedStackTrace.getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        sb.append(name)
          .append(": ")
          .append(message)
          .append("\n");
        array.forEach(s -> {
          final JsonObject object = s.getAsJsonObject();
          sb.append("\tat ")
            .append(object.get("class").getAsString())
            .append(".")
            .append(object.get("method").getAsString())
            .append("(")
            .append(object.get("file").getAsString())
            .append(":")
            .append(object.get("line").getAsInt())
            .append(")\n");
        });
        return new Thrown(message, name, sb.toString().trim());
      }
      return null;
    }
  }
}

