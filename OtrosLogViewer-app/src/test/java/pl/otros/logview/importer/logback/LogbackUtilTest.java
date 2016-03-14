package pl.otros.logview.importer.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.google.common.base.Splitter;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class LogbackUtilTest {

  private LogDataBuilder builder = new LogDataBuilder();

  @BeforeMethod
  public void beforeMethod() {
    builder = new LogDataBuilder();
  }

  @Test
  public void testTranslate() throws Exception {
    //Given
    LoggerContext context = new LoggerContext();
    Logger logger = context.getLogger("SomeLogger");
    LoggingEvent le = new LoggingEvent("a.b.C",logger, ch.qos.logback.classic.Level.INFO,"message",null,null);
    final Map<String, String> split = Splitter.on(",").withKeyValueSeparator("=").split("a=b,b=as");
    le.setMDCPropertyMap(split);
    le.setCallerData(new StackTraceElement[]{new StackTraceElement("a.b.C","someMethod","C.java",120)});

    //when
    final LogData logData = LogbackUtil.translate(le).build();

    //then
    assertEquals(logData.getMessage(),"message");
    assertEquals(logData.getLevel(),Level.INFO);
    assertEquals(logData.getLoggerName(),"SomeLogger");
    assertEquals(logData.getClazz(),"a.b.C");
    assertEquals(logData.getMethod(),"someMethod");
    assertEquals(logData.getLine(),"120");
    assertEquals(logData.getProperties(),split);
  }

  @Test
  public void testAddCallerData() throws Exception {
    //given
    final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
    final ILoggingEvent mock = mock(ILoggingEvent.class);
    when(mock.hasCallerData()).thenReturn(true);
    when(mock.getCallerData()).thenReturn(stackTrace);

    //when
    LogbackUtil.addCallerData(mock, builder);

    //then
    assertEquals(builder.build().getMethod(), "testAddCallerData");
    assertEquals(builder.build().getClazz(),getClass().getName());
  }
  @Test
  public void testAddCallerDataWithoutData() throws Exception {
    //given
    final StackTraceElement[] stackTrace = new Exception("Some exception").getStackTrace();
    final ILoggingEvent mock = mock(ILoggingEvent.class);
    when(mock.hasCallerData()).thenReturn(false);

    //when
    LogbackUtil.addCallerData(mock, builder);

    //then
    assertEquals(builder.build().getMethod(), "");
    assertEquals(builder.build().getClazz(),"");
  }

  @Test
  public void testAddException() throws Exception {
    //given
    final Exception throwable = new Exception("Some exception");
    final StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    ThrowableProxy throwableProxy = new ThrowableProxy(throwable);


    //when
    LogbackUtil.addException(throwableProxy,"Message!", builder);


    //then
    final LogData logData = builder.build();
    assertEquals(logData.getMessage(), "Message!\n" + stringWriter.getBuffer().toString());
  }

  @Test
  public void testAddNullMarker() throws Exception {
    //given
    //when
    LogbackUtil.addMarker(null, new HashMap<>(), builder);

    //then
    assertEquals(builder.build().getMarkerColors(), null);
    assertFalse(builder.build().isMarked());
  }

  @Test
  public void testAddMarker() throws Exception {
    //given
    final Marker marker = new BasicMarkerFactory().getMarker("some marker");

    //when
    LogbackUtil.addMarker(marker, new HashMap<>(), builder);

    //then
    assertEquals(builder.build().getMarkerColors(), MarkerColors.Aqua);
    assertTrue(builder.build().isMarked());
    assertEquals(builder.build().getProperties().get("marker"), "some marker");
  }


  @DataProvider(name = "levels")
  public Object[][] levels() {
    return new Object[][]{
      new Object[]{ch.qos.logback.classic.Level.DEBUG, Level.FINE},
      new Object[]{ch.qos.logback.classic.Level.TRACE, Level.FINEST},
      new Object[]{ch.qos.logback.classic.Level.INFO, Level.INFO},
      new Object[]{ch.qos.logback.classic.Level.WARN, Level.WARNING},
      new Object[]{ch.qos.logback.classic.Level.ERROR, Level.SEVERE},
      new Object[]{ch.qos.logback.classic.Level.OFF, Level.INFO},
    };
  }

  @Test(dataProvider = "levels")
  public void testConvertLevel(ch.qos.logback.classic.Level logback, Level level)
    throws Exception {
    assertEquals(LogbackUtil.convertLevel(logback), level, "Level " + logback + " should be translated to " + level);
  }
}