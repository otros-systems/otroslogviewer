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
import java.util.Stack;

/**
 * Test for PartialTextMatchRule.
 */
public class PartialTextMatchRuleTest {

    /**
     * getRule() with only one entry on stack should throw IllegalArgumentException.
     */
    @Test
    public void test1() {
	Stack<Object> stack = new Stack<>();
	stack.push("Hello");
	try {
	    PartialTextMatchRule.getRule(stack);
	    Assert.fail("Should have thrown IllegalArgumentException");
	} catch (IllegalArgumentException ignored) {
	}
    }

    /**
     * getRule() with bad field name should throw IllegalArgumentException.
     */
    @Test
    public void test2() {
	Stack<Object> stack = new Stack<>();
	stack.push("Hello");
	stack.push("World");
	try {
	    PartialTextMatchRule.getRule(stack);
	    Assert.fail("Should have thrown IllegalArgumentException");
	} catch (IllegalArgumentException ignored) {
	}
    }

    /**
     * getRule with "level" and "nfo" should return a LevelEqualsRule.
     */
    @Test
    public void test3() {
	Stack<Object> stack = new Stack<>();
	stack.push("level");
	stack.push("nfo");
	Rule rule = PartialTextMatchRule.getRule(stack);
	AssertJUnit.assertEquals(0, stack.size());
	LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", "root", System.currentTimeMillis(), Level.INFO,
		"Hello, World", null);
	AssertJUnit.assertTrue(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }

    /**
     * getRule with "msg".
     */
    @Test
    public void test4() {
	Stack<Object> stack = new Stack<>();
	stack.push("msg");
	stack.push("World");
	Rule rule = PartialTextMatchRule.getRule(stack);
	AssertJUnit.assertEquals(0, stack.size());
	LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", "root", System.currentTimeMillis(), Level.INFO,
		"Hello, World", null);
	AssertJUnit.assertTrue(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }

    /**
     * getRule with "msg".
     */
    @Test
    public void test5() {
	Stack<Object> stack = new Stack<>();
	stack.push("msg");
	stack.push("Bonjour, Monde");
	Rule rule = PartialTextMatchRule.getRule(stack);
	AssertJUnit.assertEquals(0, stack.size());
	LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", "root", System.currentTimeMillis(), Level.INFO,
		"Hello, World", null);
	AssertJUnit.assertFalse(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }

    /**
     * Check PartailTextMatchRule serialization.
     */
    @Test
    public void test6() throws IOException, ClassNotFoundException {
	Stack<Object> stack = new Stack<>();
	stack.push("msg");
	stack.push("World");
	Rule rule = (Rule) SerializationTestHelper.serializeClone(PartialTextMatchRule.getRule(stack));
	AssertJUnit.assertEquals(0, stack.size());
	LoggingEvent event = new LoggingEvent("org.apache.log4j.Logger", "root", System.currentTimeMillis(), Level.INFO,
		"Hello, World", null);
	AssertJUnit.assertTrue(rule.evaluate(Log4jUtil.translateLog4j(event), null));
    }

}
