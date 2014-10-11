package pl.otros.logview.exceptionshandler;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import pl.otros.logview.gui.StatusObserver;

public class StatusObserverExceptionHandlerTest {

  @Test
  public void testUncaughtException() throws Exception {
    //given
    StatusObserver statusObserver = Mockito.mock(StatusObserver.class);
    StatusObserverExceptionHandler statusObserverExceptionHandler = new StatusObserverExceptionHandler(statusObserver);

    //when
    statusObserverExceptionHandler.uncaughtException(Thread.currentThread(), new Exception());

    //then
    Mockito.verify(statusObserver, Mockito.times(1)).updateStatus(Mockito.anyString(), Mockito.anyInt());
  }
}