package pl.otros.logview.parser.json;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataCollector;
import pl.otros.logview.importer.InitializationException;
import pl.otros.logview.importer.LogImporterUsingParser;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.store.MemoryLogDataStore;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import static org.testng.Assert.assertEquals;

public class JsonLogParserTest {

  @DataProvider(name = "testParse")
  public Object[][] dataProvider() {
    return new Object[][]{
      new Object[]{"json.log", "Single line per log event"},
      new Object[]{"json-formatted.log", "Human readable format"},
      new Object[]{"json-formatted-withJunk.log", "Human readable format with junk lines"},
      new Object[]{"json-withJunk.log", "Single line per log event with junk lines"},
    };
  }

  @Test(dataProvider = "dataProvider")
  public void testParse(String fileName, String desc) throws Exception {
    //Given
    final Properties properties = getProperties();
    final JsonLogParser jsonParser = new JsonLogParser();
    jsonParser.init(properties);
    final ParsingContext parsingContext = new ParsingContext();
    jsonParser.initParsingContext(parsingContext);
    LogImporterUsingParser importerUsingParser = new LogImporterUsingParser(jsonParser);

    //when
    LogDataCollector ld = new MemoryLogDataStore();
    importerUsingParser.importLogs(this.getClass().getClass().getResourceAsStream("/jsonLog/" + fileName), ld, parsingContext);
    //then
    final LogData[] logDatas = ld.getLogData();
    assertEquals(logDatas.length, 5);
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
    for (int i = 0; i < logDatas.length; i++) {
      final LogData logData = logDatas[i];
      assertEquals(logData.getMessage(), "some message" + i);
      assertEquals(logData.getThread(), "main" + i);
      assertEquals(logData.getMethod(), "method" + i);
      assertEquals(logData.getFile(), "file" + i);
      assertEquals(logData.getClazz(), "a.Some" + i);
      assertEquals(logData.getLoggerName(), "logger" + i);
      assertEquals(dateFormat.format(logData.getDate()), "201" + i);
      final Map<String, String> mdc = logData.getProperties();
      assertEquals(mdc.get("appId"), "special_system" + i);
      assertEquals(mdc.get("hostname"), "host" + i);
      assertEquals(mdc.get("user"), "root" + i);
    }

    assertEquals(logDatas[0].getLevel().intValue(), Level.FINEST.intValue());
    assertEquals(logDatas[1].getLevel().intValue(), Level.FINE.intValue());
    assertEquals(logDatas[2].getLevel().intValue(), Level.INFO.intValue());
    assertEquals(logDatas[3].getLevel().intValue(), Level.WARNING.intValue());
    assertEquals(logDatas[4].getLevel().intValue(), Level.SEVERE.intValue());
  }

  private Properties getProperties() {
    final Properties properties = new Properties();
    properties.put("date", "@timestamp");
    properties.put("method", "location.method");
    properties.put("level", "level");
    properties.put("line", "location.line");
    properties.put("file", "location.file");
    properties.put("class", "location.class");
    properties.put("mdcKeys", "appId,user,hostname");
    properties.put("dateFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
    return properties;
  }

  @Test
  public void nameIsLoadedFromPropertyFile() throws InitializationException {
    //given
    final Properties properties = getProperties();
    final String name = "My Json parser";
    properties.setProperty("name", name);
    final String description = "My Json parser description";
    properties.setProperty("description", description);

    //when
    final JsonLogParser jsonParser = new JsonLogParser();
    jsonParser.init(properties);


    //then
    assertEquals(jsonParser.getName(), name);
    assertEquals(jsonParser.getDescription(), description);

  }
}