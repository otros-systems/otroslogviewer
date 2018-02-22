package pl.otros.logview.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DateUtilTest {

  private static final int SECOND = 1000;
  private static final int MINUTE = 60 * SECOND;
  private static final int HOUR = 60 * MINUTE;

  @DataProvider(name = "data")
  public Object[][] data() {
    return new Object[][]{
      {0, "0ms"},
      {SECOND, "1s"},
      {21 * SECOND, "21s"},
      {MINUTE, "1m"},
      {39 * MINUTE, "39m"},
      {HOUR, "1h"},
      {4 * HOUR, "4h"},
      {SECOND + 100, "1,1s"},
      {SECOND + 234, "1,2s"},
      {SECOND + 254, "1,3s"},
      {MINUTE + 3 * SECOND, "1m 3s"},
      {5 * MINUTE, "5m"},
      {5 * MINUTE + 3 * SECOND, "5m"},
      {HOUR + MINUTE, "1h 1m"},
      {6 * HOUR + MINUTE, "6h"},
      {-SECOND - 100, "-1,1s"},
      {-HOUR - MINUTE, "-1h 1m"},
      {-6 * HOUR - MINUTE, "-6h"},
    };

  }


  @Test(dataProvider = "data")
  public void testFormatDelta(int deltaInMs, String expected) {
    final String delta = DateUtil.formatDelta(deltaInMs);
    assertEquals(delta, expected);
  }

}