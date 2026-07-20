package pl.otros.logview.exceptionshandler;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import pl.otros.logview.api.StatusObserver;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class StatusObserverExceptionHandlerTest {

    private static class TestStatusObserver implements StatusObserver {
        String text = null;
        int level = -1;

        @Override
        public void updateStatus(String text) {
            this.text = text;
            level = -1;
        }

        @Override
        public void updateStatus(String text, int level) {
            this.text = text;
            this.level = level;
        }
    }

    @Test
    public void testUncaughtException() throws Exception {
        //given
        TestStatusObserver statusObserver = new TestStatusObserver();
        StatusObserverExceptionHandler statusObserverExceptionHandler = new StatusObserverExceptionHandler(statusObserver);

        //when
        statusObserverExceptionHandler.uncaughtException(Thread.currentThread(), new Exception());

        //then
        assertNotNull(statusObserver.text);
        assertEquals(statusObserver.level, StatusObserver.LEVEL_ERROR);
    }
}