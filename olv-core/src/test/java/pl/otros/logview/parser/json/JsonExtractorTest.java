package pl.otros.logview.parser.json;

import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import pl.otros.logview.api.model.LogData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonExtractorTest {

  @Test
  public void testMapToLogData() {
    //given
    final JsonExtractor extractor = new JsonExtractor();
    extractor.init(new Properties());
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    System.out.println(df.format(new Date(1L)));
    Map<String, String> map = ImmutableMap.<String, String>builder()
      .put(JsonExtractor.MESSAGE, "log message")
      .put(JsonExtractor.LEVEL, "")
      .put(JsonExtractor.DATE, "1970-01-01 00:00:00,001")
      .build();
    //when
    final Optional<LogData> logData = extractor.mapToLogData(map, df);

    //then
    Assert.assertTrue(logData.isPresent());
    Assert.assertEquals(logData.get().getDate().getTime(), 1L);
    Assert.assertEquals(logData.get().getMessage(), "log message");
  }

  @Test
  public void testMapToLogDataWithoutDate() {
    //given
    final JsonExtractor extractor = new JsonExtractor();
    final long currentTimeMillis = System.currentTimeMillis();
    extractor.init(new Properties());
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Map<String, String> map = ImmutableMap.<String, String>builder()
      .put(JsonExtractor.MESSAGE, "log message")
      .put(JsonExtractor.LEVEL, "")
      .build();
    //when
    final Optional<LogData> logData = extractor.mapToLogData(map, df);

    //then
    Assert.assertTrue(logData.isPresent());
    Assert.assertTrue(logData.get().getDate().getTime() >= currentTimeMillis);
  }
}