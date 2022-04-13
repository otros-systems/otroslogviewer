package pl.otros.logview.exceptionshandler.errrorreport;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ExceptionERDCTest {

  @Test
  public void testCollect() throws Exception {
    //given
    Exception exception = new Exception("msg");
    ErrorReportCollectingContext context = new ErrorReportCollectingContext();
    context.setThrowable(exception);
    context.setThread(Thread.currentThread());

    //when
    Map<String, String> collect = new ExceptionERDC().collect(context);

    //then
    assertEquals("msg",collect.get(ExceptionERDC.MESSAGE));
    assertEquals("java.lang.Exception",collect.get(ExceptionERDC.EXCEPTION));
    assertTrue(collect.containsKey(ExceptionERDC.THREAD));
    assertEquals("msg",collect.get(ExceptionERDC.MESSAGE_LOCALIZED));
    assertTrue(collect.containsKey(ExceptionERDC.STACKTRACE));
  }
}