package pl.otros.logview.accept;

import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 */
public class PropertyAcceptConditionTest {

    private LogData logData;

    @Before
    public void before(){
        Map<String,String> map = new HashMap<String, String>();
        map.put("key","value");
        logData = new LogDataBuilder().withProperties(map).build();

    }

    @Test
    public void testAccept() throws Exception {
        assertTrue(new PropertyAcceptCondition("key","value").accept(logData));
    }
    @Test
    public void testRejectKey() throws Exception {
        assertFalse(new PropertyAcceptCondition("key1","value").accept(logData));
    }
    @Test
    public void testRejectValue() throws Exception {
        assertFalse(new PropertyAcceptCondition("key","value1").accept(logData));
    }
}
