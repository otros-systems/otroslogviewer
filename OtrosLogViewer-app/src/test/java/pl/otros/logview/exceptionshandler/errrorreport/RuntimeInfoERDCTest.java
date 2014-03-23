package pl.otros.logview.exceptionshandler.errrorreport;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

/**
 */
public class RuntimeInfoERDCTest {

    private RuntimeInfoERDC runtimeInfoERDC;

    @BeforeMethod
	public void prepare() {
        runtimeInfoERDC = new RuntimeInfoERDC();
    }

    @Test
    public void testFullPattern() {
        AssertJUnit.assertEquals("4:02:10", runtimeInfoERDC.formatTimeDuration(((4 * 60 + 2) * 60 + 10) * 1000l));
    }

    @Test
    public void testFullPatternLong() {
        AssertJUnit.assertEquals("144:02:10", runtimeInfoERDC.formatTimeDuration(((144 * 60 + 2) * 60 + 10) * 1000l));
    }

    @Test
    public void testMinutesPattern() {
        AssertJUnit.assertEquals("0:02:10", runtimeInfoERDC.formatTimeDuration((2 * 60 + 10) * 1000l));
    }

    @Test
    public void testSecondsPattern() {
        AssertJUnit.assertEquals("0:00:10", runtimeInfoERDC.formatTimeDuration(10 * 1000l));
    }
}
