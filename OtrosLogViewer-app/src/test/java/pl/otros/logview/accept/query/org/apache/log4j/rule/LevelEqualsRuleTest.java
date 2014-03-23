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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import pl.otros.logview.accept.query.org.apache.log4j.util.SerializationTestHelper;
import pl.otros.logview.parser.log4j.Log4jUtil;

import java.io.IOException;

/**
 * Test for LevelEqualsRule.
 */
public class LevelEqualsRuleTest {

  /**
   * Tests evaluate when levels are equal.
   */
  @Test
  public void test1() {
    LevelEqualsRule rule = (LevelEqualsRule) LevelEqualsRule.getRule("info");
    LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", Logger.getRootLogger(), System.currentTimeMillis(), Level.INFO, "Hello, World", null);
    assertTrue(rule.evaluate(Log4jUtil.translateLog4j(event), null));
  }

  /**
   * Tests evaluate when levels are not equal.
   */
  @Test
  public void test2() {
    LevelEqualsRule rule = (LevelEqualsRule) LevelEqualsRule.getRule("info");
    LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", Logger.getRootLogger(), System.currentTimeMillis(), Level.WARN, "Hello, World", null);
    assertFalse(rule.evaluate(Log4jUtil.translateLog4j(event), null));
  }

  /**
   * Tests evaluate of a deserialized clone when levels are equal.
   */
  @Test
  public void test3() throws IOException, ClassNotFoundException {
    LevelEqualsRule rule = (LevelEqualsRule) SerializationTestHelper.serializeClone(LevelEqualsRule.getRule("info"));
    LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", Logger.getRootLogger(), System.currentTimeMillis(), Level.INFO, "Hello, World", null);
    assertTrue(rule.evaluate(Log4jUtil.translateLog4j(event), null));
  }

  /**
   * Tests evaluate of a deserialized clone when levels are not equal.
   */
  @Test
  public void test4() throws IOException, ClassNotFoundException {
    LevelEqualsRule rule = (LevelEqualsRule) SerializationTestHelper.serializeClone(LevelEqualsRule.getRule("info"));
    LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", Logger.getRootLogger(), System.currentTimeMillis(), Level.WARN, "Hello, World", null);
    assertFalse(rule.evaluate(Log4jUtil.translateLog4j(event), null));
  }

}
