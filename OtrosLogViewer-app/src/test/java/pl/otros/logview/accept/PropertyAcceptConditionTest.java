package pl.otros.logview.accept;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class PropertyAcceptConditionTest {

    private LogData logData;

    @BeforeMethod
	public void before(){
        Map<String,String> map = new HashMap<String, String>();
        map.put("key","value");
        logData = new LogDataBuilder().withProperties(map).build();

    }

    @Test
    public void testAccept() throws Exception {
        AssertJUnit.assertTrue(new PropertyAcceptCondition("key","value").accept(logData));
    }
    @Test
    public void testRejectKey() throws Exception {
        AssertJUnit.assertFalse(new PropertyAcceptCondition("key1","value").accept(logData));
    }
    @Test
    public void testRejectValue() throws Exception {
        AssertJUnit.assertFalse(new PropertyAcceptCondition("key","value1").accept(logData));
    }
}
