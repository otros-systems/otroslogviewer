package pl.otros.logview.parser.json.log4j2;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.api.reader.ProxyLogDataCollector;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.assertEquals;

public class Log4j2JsonLogParserTest {

  private LogImporter logImporter;
  private ProxyLogDataCollector logDataCollector;
  private ParsingContext parsingContext;
  private final String stacktrace = "java.lang.RuntimeException: Runtime exception!\n" +
    "\tat Log4j2Example.lambda$main$0(Log4j2Example.java:17)\n" +
    "\tat java.util.stream.Streams$RangeIntSpliterator.forEachRemaining(Streams.java:110)\n" +
    "\tat java.util.stream.IntPipeline$Head.forEach(IntPipeline.java:557)\n" +
    "\tat Log4j2Example.main(Log4j2Example.java:11)";


  @BeforeMethod
  public void setUp() throws InitializationException {
    logImporter = new LogImporterUsingParser(new Log4j2JsonLogParser());
    logImporter.init(new Properties());
    parsingContext = new ParsingContext();
    logDataCollector = new ProxyLogDataCollector();
  }

  @Test
  public void testLoadBaseLog() {
    //given
    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2/log4j-base.json");

    //when
    logImporter.importLogs(inputStream, logDataCollector, parsingContext);

    //then
    final LogData[] logData = logDataCollector.getLogData();
    assertThat(logData).hasSize(9);
    assertThat(logData[0])
      .matches(ld -> ld.getMessage().equals("Some message 0"))
      .matches(ld -> ld.getThread().equals("main"))
      .matches(ld -> ld.getLoggerName().equals("HelloWorld"))
      .matches(ld -> ld.getLevel().equals(Level.INFO))
      .matches(ld -> ld.getDate().getTime() == 1519502084529L)
      .matches(ld -> ld.getMethod().length() == 0);

    assertEquals("Exception\n" + stacktrace, logData[2].getMessage());
    assertThat(logData[2])
      .matches(ld -> ld.getMessage().equals("Exception\n" + stacktrace))
      .matches(ld -> ld.getThread().equals("main"))
      .matches(ld -> ld.getLoggerName().equals("HelloWorld"))
      .matches(ld -> ld.getLevel().equals(Level.SEVERE))
      .matches(ld -> ld.getDate().getTime() == 1519502084639L)
      .matches(ld -> ld.getMethod().length() == 0);
  }

  @Test
  public void testLoadCompleteLog() {
    //given
    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2/log4j-complete.json");

    //when
    logImporter.importLogs(inputStream, logDataCollector, parsingContext);

    //then
    final LogData[] logData = logDataCollector.getLogData();
    assertThat(logData).hasSize(9);
  }

  @Test
  public void testLoadNullDelimiterLog() {
    //given
    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2/log4j-includeNullDelimiter.json");

    //when
    logImporter.importLogs(inputStream, logDataCollector, parsingContext);

    //then
    final LogData[] logData = logDataCollector.getLogData();
    assertThat(logData).hasSize(9);
  }

  @Test
  public void testLoadLocationInfoLog() {
    //given
    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2/log4j-locationInfo.json");

    //when
    logImporter.importLogs(inputStream, logDataCollector, parsingContext);

    //then
    final LogData[] logData = logDataCollector.getLogData();
    assertThat(logData).hasSize(9);
    assertThat(logData[0])
      .matches(ld -> ld.getMethod().equals("lambda$main$0"))
      .matches(ld -> ld.getFile().equals("Log4j2Example.java"))
      .matches(ld -> ld.getLine().equals("15"))
    ;
  }

  @Test
  public void testLoadWithPropertiesLog() {
    //given
    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2/log4j-properties.json");

    //when
    logImporter.importLogs(inputStream, logDataCollector, parsingContext);

    //then
    final LogData[] logData = logDataCollector.getLogData();
    assertThat(logData).hasSize(9);
    assertThat(logData[0].getProperties())
      .matches(map -> map.get("iteration").equals("0"))
      .matches(map -> map.get("key").equals("value"))
      .matches(map -> map.size() == 2)
    ;
  }

  @Test
  public void testLoadStacktraceAsStringLog() {
    //given
    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2/log4j-stacktraceAsString.json");

    //when
    logImporter.importLogs(inputStream, logDataCollector, parsingContext);

    //then
    final LogData[] logData = logDataCollector.getLogData();
    assertThat(logData)
      .hasSize(9);
    assertThat(logData[2])
      .matches(ld -> ld.getMessage().equals(
        "Exception\n" +
          "java.lang.RuntimeException: Runtime exception!\n" +
          "\tat Log4j2Example.lambda$main$0(Log4j2Example.java:17) ~[classes/:?]\n" +
          "\tat java.util.stream.Streams$RangeIntSpliterator.forEachRemaining(Streams.java:110) [?:1.8.0_121]\n" +
          "\tat java.util.stream.IntPipeline$Head.forEach(IntPipeline.java:557) [?:1.8.0_121]\n" +
          "\tat Log4j2Example.main(Log4j2Example.java:11) [classes/:?]"));

  }

  @Test
  public void testLog4j2JsonTimeAsInstant() {
    //given
    final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("log4j2/log4j2-time-instant.json");

    //when
    logImporter.importLogs(inputStream, logDataCollector, parsingContext);

    //then
    final LogData[] logData = logDataCollector.getLogData();
    assertThat(logData).hasSize(1);
    assertThat(logData[0].getDate())
      .hasTime(1493121664118L);
  }
}