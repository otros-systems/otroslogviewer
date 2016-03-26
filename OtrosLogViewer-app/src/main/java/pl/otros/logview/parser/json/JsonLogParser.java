package pl.otros.logview.parser.json;

import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.parser.LogParser;
import pl.otros.logview.api.parser.ParserDescription;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import java.text.ParseException;
import java.util.Optional;
import java.util.Properties;

/**
 * Parser of log in json format. It can skip if between json object are some junk (from standard output). From properties
 * are taken mappings from Json ~xpath to LogData fields. For example if Json field logMessage contains information about
 * message, it should be written as message=logMessage
 */
public class JsonLogParser extends AbstractPluginableElement implements LogParser {

  private final ParserDescription parserDescription;
  private final JsonExtractor jsonExtractor;


  public JsonLogParser() {
    super("Json parser", "Json log parser");
    parserDescription = new ParserDescription();
    parserDescription.setDisplayName(name);
    parserDescription.setDescription(description);
    jsonExtractor = new JsonExtractor();
  }

  @Override
  public void init(Properties properties) throws InitializationException {
    jsonExtractor.init(properties);
    parserDescription.setDisplayName(properties.getProperty("name", "Unnamed json parser"));
    parserDescription.setDescription(properties.getProperty("description", "<Without description>"));
  }

  @Override
  public void initParsingContext(ParsingContext parsingContext) {
    parsingContext.setDateFormat(jsonExtractor.createDateFormatter());

  }

  @Override
  public LogData parse(String line, ParsingContext parsingContext) throws ParseException {

    final StringBuilder unmatchedLog = parsingContext.getUnmatchedLog();
    if (unmatchedLog.length() == 0 && !line.startsWith("{")) {
      return null;
    }

    unmatchedLog.append(line).append("\n");
    final Optional<LogData> logData = jsonExtractor.parseJsonLog(unmatchedLog.toString(), parsingContext.getDateFormat());
    if (logData.isPresent()) {
      unmatchedLog.setLength(0);
      return logData.get();
    }
    return null;
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
}

