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

import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataBuilder;
import pl.otros.logview.accept.query.org.apache.log4j.util.SerializationTestHelper;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Test for TimestampEqualsRule.
 */
public class TimestampEqualsRuleTest {

  /**
   * Tests evaluate when timestamps are equal.
   */
  @Test
  public void test1() {
    String[] timeFormats = { //
    "2008-05-21 00:45:44",//
        "2008-05-21 00:45",//
        "2008-05-21 00",//
        "2008-05-21",//
        "2008-05-21T00:45:44",//
        "2008-05-21T00:45",//
        "2008-05-21T00",//
    };

    for (String timeFormat : timeFormats) {
      TimestampEqualsRule rule = (TimestampEqualsRule) TimestampEqualsRule.getRule(timeFormat);
      LogData event = createLogData();
      AssertJUnit.assertTrue(String.format("Wrong result for time format %s", timeFormat), rule.evaluate(event, null));
    }
  }

  /**
   * Tests evaluate when levels are not equal.
   */
  @Test
  public void test2() {

    String[] timeFormats = {//
    "2008-05-21 00:45:46",//
        "2008-05-21 00:46",//
        "2008-05-21 01",//
        "2008-05-22",//
        "2008-05-21T00:45:46",//
        "2008-05-21T00:46",//
        "2008-05-21T01",//
    };

    for (String timeFormat : timeFormats) {
      TimestampEqualsRule rule = (TimestampEqualsRule) TimestampEqualsRule.getRule(timeFormat);
      LogData event = createLogData();
      AssertJUnit.assertFalse(String.format("Wrong result for time format %s", timeFormat), rule.evaluate(event, null));
    }

  }

  /**
   * Tests evaluate of a deserialized clone when timestamps are equal.
   */
  @Test
  public void test3() throws IOException, ClassNotFoundException {
    TimestampEqualsRule rule = (TimestampEqualsRule) SerializationTestHelper.serializeClone(TimestampEqualsRule.getRule("2008-05-21 00:45:44"));
    LogData event = createLogData();
    AssertJUnit.assertTrue(rule.evaluate(event, null));
  }

  /**
   * Tests evaluate of a deserialized clone when timestamps are not equal.
   */
  @Test
  public void test4() throws IOException, ClassNotFoundException {
    TimestampEqualsRule rule = (TimestampEqualsRule) SerializationTestHelper.serializeClone(TimestampEqualsRule.getRule("2008-05-21 00:46:44"));
    LogData event = createLogData();
    AssertJUnit.assertFalse(rule.evaluate(event, null));
  }

  /**
   * Tests constructor will badly formed time specification.
   */
  @Test
  public void test5() {
    try {
      TimestampEqualsRule.getRule("2008/May/21");
      Assert.fail("IllegalArgumentException expected to be thrown");
    } catch (IllegalArgumentException ignored) {
    }
  }

  private LogData createLogData() {
    return createLogData(new GregorianCalendar(2008, Calendar.MAY, 21, 0, 45, 44));
  }

  private LogData createLogData(Calendar cal) {
    return new LogDataBuilder().withClass("").withLoggerName("root").withDate(cal.getTime()).withMessage("Hi")
        .withLevel(java.util.logging.Level.INFO).build();
  }
}
