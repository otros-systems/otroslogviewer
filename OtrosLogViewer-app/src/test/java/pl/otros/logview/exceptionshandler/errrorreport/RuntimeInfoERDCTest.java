package pl.otros.logview.exceptionshandler.errrorreport;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class RuntimeInfoERDCTest {

    private RuntimeInfoERDC runtimeInfoERDC;

    @Before
    public void prepare() {
        runtimeInfoERDC = new RuntimeInfoERDC();
    }

    @Test
    public void testFullPattern() {
        Assert.assertEquals("4:02:10", runtimeInfoERDC.formatTimeDuration(((4 * 60 + 2) * 60 + 10) * 1000l));
    }

    @Test
    public void testFullPatternLong() {
        Assert.assertEquals("144:02:10", runtimeInfoERDC.formatTimeDuration(((144 * 60 + 2) * 60 + 10) * 1000l));
    }

    @Test
    public void testMinutesPattern() {
        Assert.assertEquals("0:02:10", runtimeInfoERDC.formatTimeDuration((2 * 60 + 10) * 1000l));
    }

    @Test
    public void testSecondsPattern() {
        Assert.assertEquals("0:00:10", runtimeInfoERDC.formatTimeDuration(10 * 1000l));
    }
}
