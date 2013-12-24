/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.otros.logview.accept.query.org.apache.log4j.rule;

import org.apache.log4j.Logger;
import org.junit.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.accept.query.org.apache.log4j.util.SerializationTestHelper;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

/**
 * Test for TimestampInequalityRule.
 */
public class TimestampInequalityRuleTest {

  /**
   * Test construction when timestamp is unrecognized.
   */
  @Test
  public void testFail1() {
    try {
      TimestampInequalityRule.getRule(">", "now");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException ex) {
    }
  }

  /**
   * Test construction when timestamp is unrecognized.
   */
  @Test
  public void testFail2() {
    try {
      TimestampInequalityRule.getRule(">", "2008/May/20");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException ex) {
    }
  }

  /**
   * Tests construction when operator is unrecognized.
   */
  @Test
  public void test2() {
    //
    // unlike LevelInequalityRule, does not throw exception. Resulting rule never satisified.
    //
    TimestampInequalityRule.getRule("~", "2008-05-21 00:45:46");
  }

  /**
   * Tests evaluate of a deserialized clone when rule is satisified.
   */
  @Test
  public void test3() throws IOException, ClassNotFoundException {
    Rule rule = (Rule) SerializationTestHelper.serializeClone(TimestampInequalityRule.getRule(">=", "2008-05-21 00:44:45"));
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 00, 45, 44);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  /**
   * Tests evaluate of a deserialized clone when rule is not satisfied.
   */
  @Test
  public void test4() throws IOException, ClassNotFoundException {
    Rule rule = (Rule) SerializationTestHelper.serializeClone(TimestampInequalityRule.getRule("<", "2008-05-21 00:44:44"));
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 00, 45, 44);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTBetweenDateAndTime() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">=", "2008-05-21T00:44:45");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 00, 45, 44);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTBetweenDateAndTime2() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "2008-05-21T00:44:44");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 00, 45, 44);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTBetweenDateAndTime3() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">=", "2008-05-21T00:44");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 00, 45, 44);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTBetweenDateAndTime4() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "2008-05-21T00:44");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 00, 45, 44);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTBetweenDateAndTime5() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">=", "2008-05-21T00");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 01, 45, 44);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTBetweenDateAndTime6() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "2008-05-21T00");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 00, 45, 44);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithDateOnly() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">=", "2008-05-21");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 23, 45, 44);
    LogData logData = new LogDataBuilder().withClass("a.B").withLoggerName(Logger.getRootLogger().getName()).withDate(cal.getTime()).withMessage("Hi")
        .withLevel(java.util.logging.Level.INFO).build();
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithDateOnly2() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "2008-05-21");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 23, 45, 44);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithDateOnly3() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "2008-05-22");
    Calendar cal = new GregorianCalendar(2008, Calendar.MAY, 21, 23, 45, 44);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTimeOnly() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "16:22:20");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.set(Calendar.HOUR_OF_DAY, 10);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTimeOnly2() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "16:22:20");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.set(Calendar.HOUR_OF_DAY, 18);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTimeOnly3() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "16:22:20");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.set(Calendar.HOUR_OF_DAY, 18);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTimeOnly4() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "16:22");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.set(Calendar.HOUR_OF_DAY, 10);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTimeOnly5() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule("<", "16:22");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.set(Calendar.HOUR_OF_DAY, 18);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleWithTimeOnly6() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "16:22");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.set(Calendar.HOUR_OF_DAY, 18);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus10Minutes() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-10m");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.MINUTE, -5);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus10Minutes2() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-10m");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.MINUTE, -15);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus10Minutes3() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-10min");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.MINUTE, -15);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus10Minutes4() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-10minutes");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.MINUTE, -15);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus10Minutes5() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-1minute");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.MINUTE, -15);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus2Hours() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-2h");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.HOUR_OF_DAY, -1);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus2Hours2() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-2h");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.HOUR_OF_DAY, -3);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus2Hours3() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-2hours");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.HOUR_OF_DAY, -1);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus1Hour4() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-1hour");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.MINUTE, -30);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus1Days() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-1d");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.HOUR_OF_DAY, -1);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  private LogData createLogData(Calendar cal) {
    return new LogDataBuilder().withClass("").withLoggerName(Logger.getRootLogger().getName()).withDate(cal.getTime()).withMessage("Hi")
        .withLevel(java.util.logging.Level.INFO).build();
  }

  @Test
  public void testRuleMinus2Days2() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-2d");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.DATE, -3);
    LogData logData = createLogData(cal);
    assertFalse(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus2Days3() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-2days");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.DATE, -1);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }

  @Test
  public void testRuleMinus1Day4() throws IOException, ClassNotFoundException {
    Rule rule = TimestampInequalityRule.getRule(">", "-1day");
    Calendar cal = new GregorianCalendar();
    cal.setTime(new Date(System.currentTimeMillis()));
    cal.add(Calendar.HOUR, -10);
    LogData logData = createLogData(cal);
    assertTrue(rule.evaluate(logData, null));
  }
}
