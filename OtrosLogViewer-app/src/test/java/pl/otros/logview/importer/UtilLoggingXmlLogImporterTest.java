package pl.otros.logview.importer;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.reader.ProxyLogDataCollector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class UtilLoggingXmlLogImporterTest {

  private UtilLoggingXmlLogImporter importer;

  @Before
  public void init() throws InitializationException {
    importer = new UtilLoggingXmlLogImporter();
    importer.init(new Properties());
  }

  @Test
  public void testDecodeEventsFromBeginning() throws Exception {
    //given
    String document = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("julxml/JulXmlStart.log"));
    ParsingContext context = new ParsingContext();
    importer.initParsingContext(context);
    ProxyLogDataCollector collector = new ProxyLogDataCollector();
    //when
    importer.decodeEvents(document, collector, context);
    //then
    assertEquals(3, collector.getLogData().length);
  }

  @Test
  public void testExtractFileName() {
    assertEquals("Class", importer.extractFileName("package.Class"));
    assertEquals("MyClass", importer.extractFileName("MyClass"));
    assertEquals("MyClass", importer.extractFileName("MyClass$Internal"));
    assertEquals("MyClass", importer.extractFileName("package.MyClass$Internal"));
    assertEquals("MyClass", importer.extractFileName("package.MyClass$Internal$DoubleInternal"));
    assertEquals("MyClass", importer.extractFileName("package..s.b.sdf.d.MyClass$Internal"));
    assertEquals("MyClass", importer.extractFileName("package..s.b.sdf.d.MyClass"));
  }

  @Test
  public void testAppendStackFrame() {
    assertEquals("\n\tat a.b.C.d(C.java:120)", importer.appendStackFrame(new StringBuilder(), "a.b.C", "d", "120", "C").toString());
    assertEquals("\n\tat a.b.C$I.d(C.java:120)", importer.appendStackFrame(new StringBuilder(), "a.b.C$I", "d", "120", "C").toString());
  }

  @Test
  public void testDecodeEvents() throws IOException, InitializationException {
    // given
    InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("julxml/julXmlException.xml");
    ParsingContext parsingContext = new ParsingContext("A");
    importer.initParsingContext(parsingContext);
    ProxyLogDataCollector logDataCollector = new ProxyLogDataCollector();
    String expected = "msg\n" +
                      "java.util.UnknownFormatConversionException: Conversion = 'i'\n" +
                      "\tat java.util.Formatter$FormatSpecifier.conversion(Formatter.java:2646)\n" +
                      "\tat java.util.Formatter$FormatSpecifier.<init>(Formatter.java:2675)\n" +
                      "\tat java.util.Formatter.parse(Formatter.java:2528)\n" +
                      "\tat java.util.Formatter.format(Formatter.java:2469)\n" +
                      "\tat java.util.Formatter.format(Formatter.java:2423)\n" +
                      "\tat java.lang.String.format(String.java:2797)\n" +
                      "\tat ThrowAndLog.main(ThrowAndLog.java:23)";
    // when
    importer.importLogs(resourceAsStream, logDataCollector, parsingContext);
    // then
    LogData[] logDates = logDataCollector.getLogData();
    assertEquals(1, logDates.length);
    LogData logData = logDataCollector.getLogData()[0];
    assertEquals(expected, logData.getMessage());
  }
}
