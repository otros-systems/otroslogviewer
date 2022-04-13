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

import org.apache.logging.log4j.Level;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;
import pl.otros.logview.accept.query.org.apache.log4j.util.SerializationTestHelper;
import pl.otros.logview.parser.log4j.Log4jUtil;
import pl.otros.logview.parser.log4j.LoggingEvent;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Stack;

/**
 * Test for NotRule.
 */
public class NotRuleTest  {


    /**
     * NotRule.getRule(Stack) throws exception if only one rule provided.
     */
    @Test public void test1() {
        Stack<Object> stack = new Stack<>();
        try {
            NotRule.getRule(stack);
            Assert.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * NotRule.getRule(Stack) throws exception if non-rules are provided.
     */
    @Test public void test2() {
        Stack<Object> stack = new Stack<>();
        stack.push("Hello");
        try {
            NotRule.getRule(stack);
            Assert.fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Test Not of LevelEqualsRule.
     */
    @Test public void test3() {
        Stack<Object> stack = new Stack<>();
        stack.push(LevelEqualsRule.getRule("INFO"));
        Rule rule = NotRule.getRule(stack);
        AssertJUnit.assertEquals(0, stack.size());
        Calendar cal = new GregorianCalendar(2008, 4, 21, 0, 45, 44);
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger",
                "root", cal.getTimeInMillis(), Level.INFO,
                "Hello, World", null);
        AssertJUnit.assertFalse(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }

    /**
     * Test Not of Level when Level does not match.
     */
    @Test public void test4() {
        Stack<Object> stack = new Stack<>();
        stack.push(LevelEqualsRule.getRule("INFO"));
        Rule rule = NotRule.getRule(stack);
        AssertJUnit.assertEquals(0, stack.size());
        Calendar cal = new GregorianCalendar(2008, 4, 21, 0, 45, 44);
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger",
                "root", cal.getTimeInMillis(), Level.WARN,
                "Hello, World", null);
        AssertJUnit.assertTrue(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }


    /**
     * Test deserialized Not.
     */
    @Test public void test5() throws IOException, ClassNotFoundException {
        Stack<Object> stack = new Stack<>();
        stack.push(LevelEqualsRule.getRule("INFO"));
        Rule rule = (Rule) SerializationTestHelper.serializeClone(NotRule.getRule(stack));
        AssertJUnit.assertEquals(0, stack.size());
        Calendar cal = new GregorianCalendar(2008, 4, 21, 0, 45, 44);
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger",
                "root", cal.getTimeInMillis(), Level.INFO,
                "Hello, World", null);
        AssertJUnit.assertFalse(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }


    /**
     * Test deserialized Not.
     */
    @Test public void test6() throws IOException, ClassNotFoundException {
        Stack<Object> stack = new Stack<>();
        stack.push(LevelEqualsRule.getRule("INFO"));
        Rule rule = (Rule) SerializationTestHelper.serializeClone(NotRule.getRule(stack));
        AssertJUnit.assertEquals(0, stack.size());
        Calendar cal = new GregorianCalendar(2008, 4, 21, 0, 45, 44);
        LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger",
                "root", cal.getTimeInMillis(), Level.WARN,
                "Hello, World", null);
        AssertJUnit.assertTrue(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }

}
