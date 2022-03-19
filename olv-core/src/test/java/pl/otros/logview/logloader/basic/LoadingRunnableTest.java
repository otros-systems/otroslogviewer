package pl.otros.logview.logloader.basic;

import com.google.common.collect.Range;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
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
import pl.otros.logview.parser.JulSimpleFormatterParser;
import pl.otros.logview.parser.json.JsonLogParser;
import pl.otros.logview.parser.log4j.Log4jPatternMultilineLogParser;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class LoadingRunnableTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadingRunnableTest.class);
  private static final int SLEEP_TIME = 100;
  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  private final List<String> julSimpleLogLines;
  private FileOutputStream outputStream;

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
  private final ExecutorService executorService = Executors.newFixedThreadPool(3);

  public LoadingRunnableTest() throws IOException {
    julSimpleLogLines = linesOfResources("logloader/jul-simple.log");
  }

  private List<String> linesOfResources(String resources) throws IOException {
    final InputStream jsonLogInputStream = LoadingRunnableTest.class.getClassLoader().getResourceAsStream(resources);
    return IOUtils.readLines(jsonLogInputStream, UTF_8);
  }


  @BeforeMethod
  public void setUp() throws IOException {
    collector = new ProxyLogDataCollector();
    file = File.createTempFile("LoadingRunnableTest_", ".txt");
    vfsSource = new VfsSource(VFS.getManager().toFileObject(file));
    outputStream = new FileOutputStream(file, false);

  }

  @AfterMethod
  public void tearDown() {
    IOUtils.closeQuietly(outputStream);
    underTest.stop();
  }

  @AfterClass
  public void afterClass() {
    executorService.shutdown();
  }

  @Test(invocationCount = 5)
  public void testLoadingFullWithLogParser() throws IOException {
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    underTest = createLoadingRunnable(getJulLogParser());

    executorService.submit(underTest);

    await().until(() -> collector.getCount() == 1000);
  }

  @Test(invocationCount = 5)
  public void testLoadingFullWithLogParserGzipped() throws IOException {
    saveLinesGzipped(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    underTest = createLoadingRunnable(getJulLogParser());
    executorService.submit(underTest);

    await().until(() -> collector.getCount() == 1000);
  }

  @Test(invocationCount = 5)
  public void testLoadingStopWithLogParser() throws IOException {
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);

    underTest = createLoadingRunnable(getJulLogParser());
    executorService.submit(underTest);

    await().until(() -> collector.getCount() == 1000);
    underTest.stop();
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    await().atLeast(100, TimeUnit.MILLISECONDS).until(() -> collector.getCount() == 1000);
  }


  @Test(invocationCount = 5)
  public void canDeleteFileWhenStopped() throws Exception {
    //given
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);

    //when
    underTest = new LoadingRunnable(vfsSource, getJulLogParser(), collector, 100, Optional.empty(), new EmptyStatsService());
    executorService.submit(underTest);

    await().until(() -> collector.getCount() > 0);
    outputStream.close();
    underTest.stop();

    //then
    await("File " + vfsSource.stringForm() + " should be deleted").until(() -> file.delete());
  }

    @DataProvider(name = "testLoadingAndPauseResumeDataProvider")
    public Object[][] testLoadingAndPauseResumeDataProvider() throws InitializationException {
      return new Object[][]{
        new Object[]{"Jul xml", new UtilLoggingXmlLogImporter(), "logloader/jul-xml.log", Range.closed(0, 25), 2, Range.closed(25, 48), 4},
        new Object[]{"Jul Simple", getJulLogParser(), "logloader/jul-simple.log", Range.closed(0, 10), 5, Range.closed(10, 20), 10},
        new Object[]{"lo4j pattern", getLog4jPattern(), "logloader/log4j-pattern.log", Range.closed(0, 3), 2, Range.closed(3, 8), 5},
        new Object[]{"Json", getJsonParser(), "logloader/json.log", Range.closed(0, 2), 2, Range.closed(2, 4), 4, },
        new Object[]{"autodetect-Jul xml", autoDetectLogImporter(), "logloader/jul-xml.log", Range.closed(0, 25), 2, Range.closed(25, 48), 4},
        new Object[]{"autodetect-Jul Simple", autoDetectLogImporter(), "logloader/jul-simple.log", Range.closed(0, 10), 5, Range.closed(10, 20), 10},
        new Object[]{"autodetect-lo4j pattern", autoDetectLogImporter(), "logloader/log4j-pattern.log", Range.closed(0, 8), 5, Range.closed(8, 12), 9},
        new Object[]{"autodetect-json", autoDetectLogImporter(), "logloader/json.log", Range.closed(0, 2), 2, Range.closed(2, 4), 4},
      };
    }

  private DetectOnTheFlyLogImporter autoDetectLogImporter() throws InitializationException {
    final List<LogImporter> importers = Arrays.asList(new UtilLoggingXmlLogImporter(), getJulLogParser(), getLog4jPattern(), getJsonParser());
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

  @Test(invocationCount = 5, dataProvider = "testLoadingAndPauseResumeDataProvider")
  public void testLoadingAndPauseResume(
    String name,
    LogImporter importer,
    String logSource,
    Range<Integer> range1,
    int countForFirstImport,
    Range<Integer> range2,
    int countForSecondImport)
    throws Exception {

    List<String> logLines = linesOfResources(logSource);
    LOGGER.debug("Testing " + name);
    saveLines(range1, logLines, outputStream);

    underTest = createLoadingRunnable(importer);

    executorService.submit(underTest);


    await().until(() -> collector.getCount() == countForFirstImport);
    underTest.pause();
    saveLines(range2, logLines, outputStream);
    await().atLeast(100, TimeUnit.MILLISECONDS).until(() -> collector.getCount() == countForFirstImport);
    underTest.resume();
    await().until(() -> collector.getCount() == countForSecondImport);
  }

  @Test(invocationCount = 5)
  public void testLoadingWithFilterSetOnStartWithLogParser() throws IOException {
    underTest = new LoadingRunnable(vfsSource, getJulLogParser(), collector, SLEEP_TIME, Optional.empty(), Optional.of(acceptCondition99), new EmptyStatsService());

    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    executorService.submit(underTest);

    await().until(() -> collector.getCount() == 10);
  }

  @Test(invocationCount = 5)
  public void testLoadingWithFilterSetDuringLoadWithLogParser() throws IOException {
    saveLines(Range.closed(0, 1000), julSimpleLogLines, outputStream);

    underTest = createLoadingRunnable(getJulLogParser());

    executorService.submit(underTest);

    await().until(() -> collector.getCount() == 500);
    underTest.setFilter(Optional.of(acceptCondition99));
    saveLines(Range.closed(1000, julSimpleLogLines.size()), julSimpleLogLines, outputStream);
    await().until(() -> collector.getCount() == 505);
  }

  @Test(invocationCount = 5)
  public void testLoadStatisticEmptyOnStartWithLogParser() {
    underTest = createLoadingRunnable(getJulLogParser());

    Assert.assertEquals(underTest.getLoadStatistic().getPosition(), 0);

    executorService.submit(underTest);

    await().until(() -> underTest.getLoadStatistic().getPosition() == 0);
    await().until(() -> underTest.getLoadStatistic().getTotal() == 0);
  }

  @Test(invocationCount = 5)
  public void testLoadStatisticAfterFullReadWithLogParser() throws Exception {
    underTest = createLoadingRunnable(getJulLogParser());
    saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, outputStream);

    executorService.submit(underTest);

    await().atMost(5, SECONDS).until(() -> underTest.getLoadStatistic().getPosition() == file.length());
    await().atMost(5, SECONDS).until(() -> underTest.getLoadStatistic().getTotal() == file.length());
  }


  @Test(invocationCount = 5, timeOut = 4000L)
  public void readingSocket() throws Exception {
    try (final ServerSocket serverSocket = new ServerSocket(60000);
         final Socket readingSocket = new Socket("127.0.0.1", serverSocket.getLocalPort());
         final Socket writingSocket = serverSocket.accept()
    ) {
      System.out.println(readingSocket.isClosed());
      SocketSource socketSource = new SocketSource(readingSocket);
      saveLines(Range.closed(0, julSimpleLogLines.size()), julSimpleLogLines, writingSocket.getOutputStream());

      underTest = new LoadingRunnable(socketSource, getJulLogParser(), collector, SLEEP_TIME, Optional.empty(), Optional.empty(), new EmptyStatsService());

      executorService.submit(underTest);

      await().until(() -> collector.getCount() == 999);//last line is not read
      underTest.stop();

      final LoadStatistic loadStatistic = underTest.getLoadStatistic();
      LOGGER.info("Have loading statistic {}", loadStatistic);
      Assert.assertEquals(loadStatistic.getPosition(), 0);
      Assert.assertEquals(loadStatistic.getTotal(), 0);
    }

  }


  private LogImporterUsingParser getJulLogParser() {
    return new LogImporterUsingParser(new JulSimpleFormatterParser());
  }

  private LogImporter getJsonParser() {
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
    IOUtils.write(stringBuilder.toString(), outputStream, UTF_8);
    outputStream.flush();
  }

  private void saveLinesGzipped(Range<Integer> range, List<String> logLines, OutputStream outputStream) throws IOException {
    try (GZIPOutputStream tmp = new GZIPOutputStream(outputStream)) {
      saveLines(range, logLines, tmp);
    }
  }

  @Nonnull
  private LoadingRunnable createLoadingRunnable(LogImporter logImporter) {
    return new LoadingRunnable(vfsSource, logImporter, collector, SLEEP_TIME, Optional.empty(), new EmptyStatsService());
  }
}