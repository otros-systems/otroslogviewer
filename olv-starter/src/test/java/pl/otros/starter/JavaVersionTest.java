package pl.otros.starter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static  org.testng.Assert.*;

public class JavaVersionTest {

  @DataProvider(name = "fromString")
  public Object[][] fromStringDataProvider(){
    return new Object[][] {
      new Object[]{"1.2.3_23", new JavaVersion(1,2,3)},
      new Object[]{"1.8.24_3sdf",new JavaVersion(1,8,24)},
    };
  }

  @Test(dataProvider = "fromString")
  public void testFromString(String string, JavaVersion version) throws Exception {
    assertEquals(JavaVersion.fromString(string),version);
  }

  @Test
  public void testCompareTo() throws Exception {
    assertEquals(new JavaVersion(1,9,2).compareTo(new JavaVersion(1,9,2)),0);
    assertTrue(new JavaVersion(1,9,2).compareTo(new JavaVersion(1,7,2))>0);
    assertTrue(new JavaVersion(1,5,2).compareTo(new JavaVersion(1,7,2))<0);
  }
}