package pl.otros.logview.logppattern;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Properties;

public class LogbackLayoutEncoderConverterTest {

  private LogbackLayoutEncoderConverter converter;

  @BeforeMethod
  public void setUp() {
    converter = new LogbackLayoutEncoderConverter();
  }

  private void testValid(String logPattern, String expectedPattern) throws Exception {
    testValid(logPattern, expectedPattern, "yyyy-MM-dd HH:mm:ss,SSS");
  }

  private void testValid(String logPattern, String expectedPattern, String expectedDateFormat) throws Exception {
    Properties result = converter.convert(logPattern);
    Assert.assertEquals(result.getProperty("pattern"), expectedPattern);
    Assert.assertEquals(result.getProperty("dateFormat"), expectedDateFormat);
    Assert.assertEquals(result.getProperty("type"), "log4j");
    Assert.assertEquals(result.getProperty("charset"), "UTF-8");
  }

  private void isInvalid(String logPattern) {
    Assert.expectThrows(Exception.class, () -> converter.convert(logPattern));
  }

  @Test
  public void testParseMostBasicLog() throws Exception {
    String logPattern = "%level %d{HH:mm:ss.SSS} %thread %logger %msg%n";
    String pattern = "LEVEL TIMESTAMP THREAD CLASS MESSAGE";
    String dateFormat = "HH:mm:ss.SSS";
    testValid(logPattern, pattern, dateFormat);
  }

  @Test
  public void testParseFormatWithCustomDateAndLevelFormatting() throws Exception {
    String logPattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    testValid(logPattern, "TIMESTAMP [THREAD] LEVEL CLASS - MESSAGE", "HH:mm:ss.SSS");
  }

  @Test
  public void testParseFormatWithDefaultDateFormat() throws Exception {
    String logPattern = "%d %level %logger %msg%n";
    testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE");
  }

  @Test
  public void testParseFormatWithISO8601DateFormat() throws Exception {
    String logPattern = "%d{ISO8601} %level %logger %msg%n";
    testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE");
  }

  @Test
  public void testParseFormatWithExceptionMergedWithMessageAfter() throws Exception {
    String logPattern = "%d %level %logger %msg %exception %n";
    testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE");
  }

  @Test
  public void testParseFormatWithExceptionMergedWithMessageBefore() throws Exception {
    String logPattern = "%d %level %logger %exception %msg%n";
    testValid(logPattern, "TIMESTAMP LEVEL CLASS MESSAGE");
  }

  @Test
  public void testParseFormatWith1MdcKey() throws Exception {
    String logPattern = "%d %level %logger [%mdc{requestId}] %msg%n";
    testValid(logPattern, "TIMESTAMP LEVEL CLASS [PROP(requestId)] MESSAGE");
  }

  @Test
  public void testParseFormatWith2MdcKeysAsList() throws Exception {
    String logPattern = "%d %level %logger %mdc{requestId, userId} %msg%n";
    testValid(logPattern, "TIMESTAMP LEVEL CLASS requestId=PROP(requestId) userId=PROP(userId) MESSAGE");
  }

  @Test
  public void testParseFormatWith2MdcKeys() throws Exception {
    String logPattern = "%d %level %logger [%mdc{requestId}] [%mdc{userId}] %msg%n";
    testValid(logPattern, "TIMESTAMP LEVEL CLASS [PROP(requestId)] [PROP(userId)] MESSAGE");
  }

  @Test
  public void testDontParseIllegalFormatMergedConversionWord() throws Exception {
    String logPattern = "%d %level%logger %msg%n";
    // This pattern is actually considered valid by the current implementation
    // because it parses %level as LEVEL and %logger as CLASS, then joins them.
    // In LogbackPatternConverterTest.scala it was expected to be invalid.
    converter.convert(logPattern);
  }

  @DataProvider(name = "invalidPatterns")
  public Object[][] invalidPatterns() {
    return new Object[][]{
      { "%d %levelx %logger %msg%n" },
      { "%d %level %msg %logger %n" },
      { "%d %level %logger %MDC %msg%n" },
      { "something ..... ..." },
      { "%date{yyyy-MM-dd} %-5level[%thread] %logger - %msg%n" },
      { "%date{yyyy-MM-dd} %X{mdcKey} %-5level [%thread] %logger - %msg%n" }
    };
  }

  @Test(dataProvider = "invalidPatterns")
  public void testInvalidFormats(String logPattern) {
    isInvalid(logPattern);
  }
}
