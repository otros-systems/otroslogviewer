package pl.otros.logview.exceptionshandler.errrorreport;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static pl.otros.logview.exceptionshandler.errrorreport.SystemPropertiesERDC.PREFIX;

public class SystemPropertiesERDCTest {

  private static final String KEY1 = "testProp1";
  private static final String KEY2 = "testProp2";
  private static final String KEY3 = "testProp3-password";
  private static final String KEY4 = "user.name";
  private SystemPropertiesERDC erdc;

  @BeforeTest
  public void before(){
    erdc = new SystemPropertiesERDC();
    System.setProperty(KEY1,"value1");
    System.setProperty(KEY2,"value2");
    System.setProperty(KEY3,"some password");
    System.setProperty(KEY4,"John Doe");
  }

  @AfterTest
  public void after(){
    System.clearProperty(KEY1);
    System.clearProperty(KEY2);
    System.clearProperty(KEY3);
    System.clearProperty(KEY4);
  }


  @Test
  public void testCollect() {
    //given
    //when
    Map<String, String> collect = erdc.collect(null);

    //then
    assertEquals(collect.get(PREFIX+KEY1), "value1");
    assertEquals(collect.get(PREFIX+KEY2), "value2");
  }

  @Test
  public void testFillValues() {
 //given
    Properties p = new Properties();
    p.setProperty("a","b");
    p.setProperty("c","d");
    HashMap<String, String> map = new HashMap<>();

    //when
    erdc.fillValues(map, p);

    //then
    assertEquals(map.get(PREFIX+"a"), "b");
    assertEquals(map.get(PREFIX+"c"), "d");
  }
}