package pl.otros.logview.logloader.basic;

import com.google.common.collect.Range;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.accept.AbstractAcceptContidion;
import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.importer.LogImporterUsingParser;
import pl.otros.logview.api.loading.LoadStatistic;
import pl.otros.logview.api.loading.SocketSource;
import pl.otros.logview.api.loading.Source;
import pl.otros.logview.api.loading.VfsSource;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.reader.ProxyLogDataCollector;
import pl.otros.logview.api.services.EmptyStatsService;
import pl.otros.logview.importer.DetectOnTheFlyLogImporter;
import pl.otros.logview.importer.UtilLoggingXmlLogImporter;
import pl.otros.logview.importer.log4jxml.Log4jXmlLogImporter;
import pl.otros.logview.parser.JulSimpleFormatterParser;
import pl.otros.logview.parser.json.JsonLogParser;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class LoadingRunnableTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadingRunnableTest.class);
  public static final int SLEEP_TIME = 100;

  private final List<String> julSimpleLogLines;
  private final List<String> julXmlLogLines;
  private final List<String> log4jXmlLogLines;
  private final List<String> log4jLogLines;
  private final List<String> jsonLogLines;
  private FileOutputStream outputStream;
  private LogImporter logImporter;

  private LogDataCollector collector;
  private File file;
  private Source vfsSource;
  private LoadingRunnable underTest;
  private final AcceptCondition acceptCondition99 = new AbstractAcceptContidion() {
    @Override
    public boolean accept(LogData data) {
      return data.getMessage().endsWith("99");
    }
  };

  public LoadingRunnableTest() throws IOException {
    final InputStream julSimpleLogInputStream = LoadingRunnableTest.class.getClassLoader().getResourceAsStream("logloader/jul-simple.log");
    julSimpleLogLines = IOUtils.readLines(julSimpleLogInputStream);

    final InputStream julXmlLogInputStream = LoadingRunnableTest.class.getClassLoader().getResourceAsStream("logloader/jul-xml.log");
    julXmlLogLines = IOUtils.readLines(julXmlLogInputStream);

    final InputStream log4jXmlLogInputStream = LoadingRunnableTest.class.getClassLoader().getResourceAsStream("logloader/log4j-xml.log");
    log4jXmlLogLines = IOUtils.readLines(log4jXmlLogInputStream);

    final InputStream log4jLogInputStream = LoadingRunnableTest.class.getClassLoader().getResourceAsStream("logloader/log4j-pattern.log");
    log4jLogLines = IOUtils.readLines(log4jLogInputStream);

    final InputStream jsonLogInputStream = LoadingRunnableTest.class.getClassLoader().getResourceAsStream("logloader/json.log");
    jsonLogLines = IOUtils.readLines(jsonLogInputStream);
  }


  @BeforeMethod
  public void setUp() throws IOException {
    collector = new ProxyLogDataCollector();
    file = File.createTempFile("LoadingRunnableTest_", ".txt");
    vfsSource = new VfsSource(VFS.getManager().toFileObject(file), 0);
    outputStream = new FileOutputStream(file, false);

  }

  @AfterMethod
  public void tearDown() {
    IOUtils.closeQuietly(outputStream);
    underTest.stop();
  }

  @Test
  public void testLoadingFullWithLogParser() throws IOException, InterruptedException {
    logImporter = getJulLogParser();
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    underTest = new LoadingRunnable(vfsSource, logImporter, collector, SLEEP_TIME, Optional.empty(), new EmptyStatsService());
    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, 1000);

  }

  @Test
  public void testLoadingFullWithLogParserGzipped() throws IOException, InterruptedException {
    logImporter = getJulLogParser();
    saveLinesGzipped(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    underTest = new LoadingRunnable(vfsSource, logImporter, collector, SLEEP_TIME, Optional.empty(),new EmptyStatsService());
    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, 1000);

  }

  @Test
  public void testLoadingStopWithLogParser() throws IOException, InterruptedException {
    logImporter = getJulLogParser();
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);

    underTest = new LoadingRunnable(vfsSource, logImporter, collector, SLEEP_TIME, Optional.empty(),new EmptyStatsService());
    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, 1000);

    underTest.stop();
    Thread.sleep(100);
  }


  @Test
  public void canDeleteFileWhenStopped() throws Exception {
    //given
    logImporter = getJulLogParser();
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);

    //when
    underTest = new LoadingRunnable(vfsSource, logImporter, collector, 50, Optional.empty(),new EmptyStatsService());
    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(400);
    outputStream.close();
    underTest.stop();
    Thread.sleep(100);

    //then
    Assert.assertTrue(file.delete(), "File " + vfsSource.stringForm() + " should be deleted");
  }

  @DataProvider(name = "testLoadingAndPauseResumeDataProvider")
  public Object[][] testLoadingAndPauseResumeDataProvider() throws InitializationException {
    return new Object[][]{
        new Object[]{"Jul xml", new UtilLoggingXmlLogImporter(), this.julXmlLogLines, Range.closed(0, 25), 2, Range.closed(25, 48), 4, Range.closed(48, 70), 6},
        new Object[]{"Jul Simple", getJulLogParser(), this.julSimpleLogLines, Range.closed(0, 10), 5, Range.closed(10, 20), 10, Range.closed(20, 30), 15},
        new Object[]{"lo4j xml", new Log4jXmlLogImporter(), this.log4jXmlLogLines, Range.closed(0, 8), 2, Range.closed(8, 12), 3, Range.closed(12, 23), 5},
        new Object[]{"lo4j pattern", getLog4jPattern(), this.log4jLogLines, Range.closed(0, 3), 2, Range.closed(3, 8), 5, Range.closed(8, 12), 9},
        new Object[]{"Json", getJsonParser(), this.jsonLogLines, Range.closed(0, 2), 2, Range.closed(2, 4), 4, Range.closed(4, 5), 5},
        new Object[]{"autodetect-Jul xml", autoDetectLogImporter(), this.julXmlLogLines, Range.closed(0, 25), 2, Range.closed(25, 48), 4, Range.closed(48, 70), 6},
        new Object[]{"autodetect-Jul Simple", autoDetectLogImporter(), this.julSimpleLogLines, Range.closed(0, 10), 5, Range.closed(10, 20), 10, Range.closed(20, 30), 15},
        new Object[]{"autodetect-lo4j xml", autoDetectLogImporter(), this.log4jXmlLogLines, Range.closed(0, 8), 2, Range.closed(8, 12), 3, Range.closed(12, 23), 5},
        new Object[]{"autodetect-lo4j pattern", autoDetectLogImporter(), this.log4jLogLines, Range.closed(0, 8), 5, Range.closed(8, 12), 9, Range.closed(12, 16), 13},
        new Object[]{"autodetect-json", autoDetectLogImporter(), this.jsonLogLines, Range.closed(0, 2), 2, Range.closed(2, 4), 4, Range.closed(4, 5), 5},
    };
  }

  private DetectOnTheFlyLogImporter autoDetectLogImporter() throws InitializationException {
    final List<LogImporter> importers = Arrays.asList(new UtilLoggingXmlLogImporter(), getJulLogParser(), new Log4jXmlLogImporter(), getLog4jPattern(), getJsonParser());
    return new DetectOnTheFlyLogImporter(importers);
  }

  private LogImporter getLog4jPattern() throws InitializationException {
    Log4jPatternMultilineLogParser log4jPatternMultilineLogParser = new Log4jPatternMultilineLogParser();
    Properties p = new Properties();
    p.put("type", "log4j");
    p.put("pattern", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("dateFormat", "yyyy-MM-dd HH:mm:ss,SSS");
    p.put("name", "TIMESTAMP LEVEL [THREAD]  MESSAGE");
    p.put("charset", "UTF-8");

    LogImporterUsingParser logImporterUsingParser = new LogImporterUsingParser(log4jPatternMultilineLogParser);
    logImporterUsingParser.init(p);
    return logImporterUsingParser;
  }

  @Test(dataProvider = "testLoadingAndPauseResumeDataProvider")
  public void testLoadingAndPauseResume(String name, LogImporter importer, List<String> logLines, Range<Integer> range1, int countForFirstImport, Range<Integer> range2, int countForSecondImport, Range<Integer> range3, int countForThirdImport) throws Exception {
    LOGGER.debug("Testing " + name);
    saveLines(range1, logLines, outputStream);

    underTest = new LoadingRunnable(vfsSource, importer, collector, 100, Optional.empty(),new EmptyStatsService());
    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();

    Thread.sleep(300);

    final int length = collector.getLogData().length;
    LOGGER.debug("Have {} elements", length);
    Assert.assertEquals(length, countForFirstImport);

    underTest.pause();
    saveLines(range2, logLines, outputStream);
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, countForFirstImport);
    underTest.resume();
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, countForSecondImport);

    saveLines(range3, logLines, outputStream);
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, countForThirdImport);
  }

  @Test
  public void testLoadingWithFilterSetOnStartWithLogParser() throws IOException, InterruptedException {
    logImporter = getJulLogParser();
    underTest = new LoadingRunnable(vfsSource, logImporter, collector, 100, Optional.empty(), Optional.of(acceptCondition99),new EmptyStatsService());

    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, 10);
  }

  @Test
  public void testLoadingWithFilterSetDuringLoadWithLogParser() throws IOException, InterruptedException {
    logImporter = getJulLogParser();
    saveLines(Range.closed(0, 1000), julSimpleLogLines, outputStream);

    underTest = new LoadingRunnable(vfsSource, logImporter, collector, 100, Optional.empty(), Optional.empty(),new EmptyStatsService());

    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, 500);
    underTest.setFilter(Optional.of(acceptCondition99));
    saveLines(Range.closed(1000, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    Thread.sleep(300);
    Assert.assertEquals(collector.getLogData().length, 505);
  }

  @Test
  public void testLoadStatisticEmptyOnStartWithLogParser() throws Exception {
    logImporter = getJulLogParser();
    underTest = new LoadingRunnable(vfsSource, logImporter, collector, 100, Optional.empty(), Optional.empty(),new EmptyStatsService());

    Assert.assertEquals(underTest.getLoadStatistic().getPosition(), 0);

    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();
    Thread.sleep(300);

    Assert.assertEquals(underTest.getLoadStatistic().getPosition(), 0);
    Assert.assertEquals(underTest.getLoadStatistic().getTotal(), 0);
  }

  @Test(invocationCount = 10)
  public void testLoadStatisticAfterFullReadWithLogParser() throws Exception {
    logImporter = getJulLogParser();
    underTest = new LoadingRunnable(vfsSource, logImporter, collector, SLEEP_TIME, Optional.empty(), Optional.empty(),new EmptyStatsService());
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    
    final Thread thread = new Thread(underTest);
    thread.setDaemon(true);
    thread.start();

    await().atMost(5, SECONDS).until(() -> underTest.getLoadStatistic().getPosition() == file.length());
    await().atMost(5, SECONDS).until(() -> underTest.getLoadStatistic().getTotal() == file.length());
  }


  @Test(timeOut = 4000L)
  public void readingSocket() throws Exception {
    try (final ServerSocket serverSocket = new ServerSocket(60000);
         final Socket readingSocket = new Socket("127.0.0.1", serverSocket.getLocalPort());
         final Socket writingSocket = serverSocket.accept()
    ) {
      System.out.println(readingSocket.isClosed());
      SocketSource socketSource = new SocketSource(readingSocket);
      saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, writingSocket.getOutputStream());

      logImporter = getJulLogParser();
      underTest = new LoadingRunnable(socketSource, logImporter, collector, 100, Optional.empty(), Optional.empty(),new EmptyStatsService());

      final Thread thread = new Thread(underTest);
      thread.setDaemon(true);
      thread.start();
      Thread.sleep(900);

      underTest.stop();

      Assert.assertEquals(collector.getLogData().length, 999); //last line is not read
      final LoadStatistic loadStatistic = underTest.getLoadStatistic();
      LOGGER.info("Have loading statistic {}", loadStatistic);
      Assert.assertEquals(loadStatistic.getPosition(), 0);
      Assert.assertEquals(loadStatistic.getTotal(), 0);
    }

  }


  private LogImporterUsingParser getJulLogParser() {
    return new LogImporterUsingParser(new JulSimpleFormatterParser());
  }

  private LogImporter getJsonParser() throws InitializationException {
    final Properties properties = new Properties();
    properties.put("date", "@timestamp");
    properties.put("method", "location.method");
    properties.put("level", "level");
    properties.put("line", "location.line");
    properties.put("file", "location.file");
    properties.put("class", "location.class");
    properties.put("mdcKeys", "appId,user,hostname");
    properties.put("dateFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");

    final JsonLogParser jsonParser = new JsonLogParser();
    jsonParser.init(properties);
    return new LogImporterUsingParser(jsonParser);
  }

  private void saveLines(Range<Integer> range, List<String> logLines, OutputStream outputStream) throws IOException {
    int start = range.lowerEndpoint();
    int end = range.upperEndpoint();

    final List<String> strings = logLines.subList(start, end);
    final Iterator<String> iterator = strings.iterator();
    final StringBuilder stringBuilder = new StringBuilder();
    while (iterator.hasNext()) {
      stringBuilder.append(iterator.next()).append("\n");
    }
    IOUtils.write(stringBuilder, outputStream);
    outputStream.flush();
  }

  private void saveLinesGzipped(Range<Integer> range, List<String> logLines, OutputStream outputStream) throws IOException {
    try(GZIPOutputStream tmp = new GZIPOutputStream(outputStream)) {
      saveLines(range, logLines, tmp);
    }
  }
}