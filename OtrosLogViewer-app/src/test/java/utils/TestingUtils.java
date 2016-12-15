package utils;

import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;

import java.util.Properties;

public class TestingUtils {

  public static LogImporterAndFile log4jPatternImporterAndFile() throws InitializationException {
    Log4jPatternMultilineLogParser log4jPatternMultilineLogParser = new Log4jPatternMultilineLogParser();
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
    p.put("name", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("charset", "UTF-8");

    LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(log4jPatternMultilineLogParser);
    logImporterUsingParser.init(p);

    return new LogImporterAndFile(logImporterUsingParser,"log4j.txt");
  }
}
