package pl.otros.logview.importer;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.reader.ProxyLogDataCollector;

import java.io.InputStream;
import java.util.Properties;

public class UtilLoggingXmlLogImporterTest {

  @Test
  public void testDecodeEventsFromBeginning() throws Exception {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("./julxml/JulXmlStart.log");
    String document = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("julxml/JulXmlStart.log"));

    UtilLoggingXmlLogImporter importer = new UtilLoggingXmlLogImporter();
    importer.init(new Properties());
    ParsingContext context = new ParsingContext();

    importer.initParsingContext(context);
    ProxyLogDataCollector collector = new ProxyLogDataCollector();

    importer.decodeEvents(document, collector, context);

    Assert.assertEquals(3, collector.getLogData().length);

  }
}
