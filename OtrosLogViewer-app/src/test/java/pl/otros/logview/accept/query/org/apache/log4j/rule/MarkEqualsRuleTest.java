package pl.otros.logview.accept.query.org.apache.log4j.rule;

import org.junit.Before;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MarkEqualsRuleTest {

    private LogData ldNotMarked;
    private LogData ldBlack;
    private LogData ldBrown;

    @Before
    public void prepare() {
	ldBrown = new LogDataBuilder().withMarkerColors(MarkerColors.Brown).build();
	ldBrown.setMarked(true);
	ldBlack = new LogDataBuilder().withMarkerColors(MarkerColors.Black).build();
	ldBlack.setMarked(true);
	ldNotMarked = new LogDataBuilder().build();

    }

    @Test
    public void testEvaluateColorPostive() {
	// given
	Rule rule = MarkEqualsRule.getRule(MarkerColors.Black.toString(),false);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertTrue(evaluate);
    }

    @Test
    public void testEvaluateColorNegative() {
	// given
	Rule rule = MarkEqualsRule.getRule("brown",false);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertFalse(evaluate);
    }

    @Test
    public void testEvaluateMarkedWithMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("true",false);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertTrue(evaluate);
    }

    @Test
    public void testEvaluateMarkedWithNotMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("true",false);
	
	// when
	boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<Object, Object>());
	
	// then
	assertFalse(evaluate);
    }

    @Test
    public void testEvaluateNotMarkedWithMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("false",false);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertFalse(evaluate);
    }
    
    @Test
    public void testEvaluateNotMarkedWithNotMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("false",false);
	
	// when
	boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<Object, Object>());
	
	// then
	assertTrue(evaluate);
    }
    
    
    
    @Test
    public void testNegationEvaluateColorPostive() {
	// given
	Rule rule = MarkEqualsRule.getRule(MarkerColors.Black.toString(),true);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertTrue(evaluate);
    }

    @Test
    public void testNegationEvaluateColorNegative() {
	// given
	Rule rule = MarkEqualsRule.getRule("brown",true);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertFalse(evaluate);
    }

    @Test
    public void testNegationEvaluateMarkedWithMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("true",true);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertTrue(evaluate);
    }

    @Test
    public void testNegationEvaluateMarkedWithNotMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("true",true);
	
	// when
	boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<Object, Object>());
	
	// then
	assertFalse(evaluate);
    }

    @Test
    public void testNegationEvaluateNotMarkedWithMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("false",true);

	// when
	boolean evaluate = rule.evaluate(ldBlack, new HashMap<Object, Object>());

	// then
	assertFalse(evaluate);
    }
    
    @Test
    public void testNegationEvaluateNotMarkedWithNotMarked() {
	// given
	Rule rule = MarkEqualsRule.getRule("false",true);
	
	// when
	boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<Object, Object>());
	
	// then
	assertTrue(evaluate);
    }
    
    
    

}
