package pl.otros.logview.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UnixProcessingTest {


  @DataProvider(name = "processText")
  public Object[][] dataProvider() {
    return new Object[][]{
      new Object[]{"ala\nma\nkota", "grep ma", "ma"},
      new Object[]{"ala\nma\nkota", "grep -v ma", "ala\nkota"},
      new Object[]{"ala\nma\nkota", "cut -c1-3", "ala\nma\nkot"},
      new Object[]{"ala ma kota", "cut -c3-", "a ma kota"},
      new Object[]{"ala ma kota", "cut -c-3", "ala"},
      new Object[]{"ala\nma\nkota", "cut -c 1-3", "ala\nma\nkot"},
      new Object[]{"ala ma kota", "cut -c 3-", "a ma kota"},
      new Object[]{"ala ma kota", "cut -c -3", "ala"},
      new Object[]{"ala ma kota", "cut -c1,2,5", "alm"},
      new Object[]{"ala ma kota", "cut -c 1,2,5", "alm"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d, -f2", "ma"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d, -f2,5", "ma,kot"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d, -f-2", "ala,ma"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d, -f2-", "ma,kota,a,kot,ma,ale"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d , -f 2", "ma"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d , -f 2,5", "ma,kot"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d , -f -2", "ala,ma"},
      new Object[]{"ala,ma,kota,a,kot,ma,ale", "cut -d , -f 2-", "ma,kota,a,kot,ma,ale"},
      new Object[]{"ala,ma,kota {a,kot,ma,ale", "cut -d\\{ -f 2", "a,kot,ma,ale"},
//      new Object[]{"ala ma kota  a kot ma ale", "cut -d\\  -f 2", "ma"},
      new Object[]{"ala ma kota", "sed s/ma/XX/g", "ala XX kota"},
      new Object[]{"ala\nma\nkota", "grep -v ma | sed s/a/G/g", "GlG\nkotG"},
    };
  }

  @Test(dataProvider = "processText")
  public void testProcessText(String text, String cli, String expected) {
    final String actual = new UnixProcessing().processText(text, cli);
    assertEquals(actual, expected);
  }

}