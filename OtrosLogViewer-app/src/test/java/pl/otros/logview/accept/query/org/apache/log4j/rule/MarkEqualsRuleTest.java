package pl.otros.logview.accept.query.org.apache.log4j.rule;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;

import java.util.HashMap;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class MarkEqualsRuleTest {

  private LogData ldNotMarked;
  private LogData ldBlack;

  @BeforeMethod
  public void prepare() {
    ldBlack = new LogDataBuilder().withMarkerColors(MarkerColors.Black).build();
    ldBlack.setMarked(true);
    ldNotMarked = new LogDataBuilder().build();

  }

  @Test
  public void testEvaluateColorPostive() {
    // given
    Rule rule = MarkEqualsRule.getRule(MarkerColors.Black.toString());

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertTrue(evaluate);
  }

  @Test
  public void testEvaluateColorNegative() {
    // given
    Rule rule = MarkEqualsRule.getRule("brown");

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertFalse(evaluate);
  }

  @Test
  public void testEvaluateMarkedWithMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("true");

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertTrue(evaluate);
  }

  @Test
  public void testEvaluateMarkedWithNotMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("true");

    // when
    boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<>());

    // then
    assertFalse(evaluate);
  }

  @Test
  public void testEvaluateNotMarkedWithMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("false");

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertFalse(evaluate);
  }

  @Test
  public void testEvaluateNotMarkedWithNotMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("false");

    // when
    boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<>());

    // then
    assertTrue(evaluate);
  }


  @Test
  public void testNegationEvaluateColorPostive() {
    // given
    Rule rule = MarkEqualsRule.getRule(MarkerColors.Black.toString());

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertTrue(evaluate);
  }

  @Test
  public void testNegationEvaluateColorNegative() {
    // given
    Rule rule = MarkEqualsRule.getRule("brown");

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertFalse(evaluate);
  }

  @Test
  public void testNegationEvaluateMarkedWithMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("true");

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertTrue(evaluate);
  }

  @Test
  public void testNegationEvaluateMarkedWithNotMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("true");

    // when
    boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<>());

    // then
    assertFalse(evaluate);
  }

  @Test
  public void testNegationEvaluateNotMarkedWithMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("false");

    // when
    boolean evaluate = rule.evaluate(ldBlack, new HashMap<>());

    // then
    assertFalse(evaluate);
  }

  @Test
  public void testNegationEvaluateNotMarkedWithNotMarked() {
    // given
    Rule rule = MarkEqualsRule.getRule("false");

    // when
    boolean evaluate = rule.evaluate(ldNotMarked, new HashMap<>());

    // then
    assertTrue(evaluate);
  }


}
