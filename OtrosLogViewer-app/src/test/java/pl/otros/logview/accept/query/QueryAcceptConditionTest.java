/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package pl.otros.logview.accept.query;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pl.otros.logview.LogData;
import pl.otros.logview.LogDataBuilder;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.Note;
import pl.otros.logview.accept.query.org.apache.log4j.rule.RuleException;

import java.util.*;
import java.util.logging.Level;

import static org.testng.AssertJUnit.assertEquals;

public class QueryAcceptConditionTest {

	private LogData ldCurrentTimeLevelInfo;
	private LogData ld_2011_12_01_120000;
	private LogData ldWarning;
	private LogData ldWithProperties;
	private LogData ldClassMethod;
	private LogData ldMarkedNoted;

	@BeforeMethod
	public void prepare() throws RuleException {
		ldCurrentTimeLevelInfo = new LogDataBuilder().withMessage("ab")
				.withId(2).withDate(new Date()).withLevel(Level.INFO).build();
		Calendar cal = new GregorianCalendar(2011, 12, 1, 12, 0, 0);
		ld_2011_12_01_120000 = new LogDataBuilder().withMessage("a").withId(2)
				.withDate(new Date(cal.getTimeInMillis()))
				.withLevel(Level.INFO).build();
		ldWarning = new LogDataBuilder().withMessage("vab").withId(2)
				.withDate(new Date()).withLevel(Level.WARNING).build();
		Map<String, String> properties = new HashMap<>();
		properties.put("key", "value");
		properties.put("secondKey", "value2");
		ldWithProperties = new LogDataBuilder().withMessage("with properties")
				.withId(2).withDate(new Date()).withLevel(Level.WARNING)
				.withProperties(properties).build();

		ldClassMethod = new LogDataBuilder().withMessage("c").withId(2)
				.withDate(new Date()).withLevel(Level.WARNING)
				.withClass("a.b.C").withMethod("myMethod").build();

		ldMarkedNoted = new LogDataBuilder().withMessage("c").withId(2)
				.withDate(new Date()).withLevel(Level.WARNING).withMarked(true)
				.withMarkerColors(MarkerColors.Brown)
				.withNote(new Note("ala ma kota")).build();

	}

	@Test
	public void testMarked() throws RuleException {
		testRule("mark==false", ldMarkedNoted, false);
		testRule("mark==true", ldMarkedNoted, true);
		testRule("mark==brown", ldMarkedNoted, true);
		testRule("mark==pink", ldMarkedNoted, false);
	}

	@Test
	public void testNoted() throws RuleException {
		testRule("note==abcd", ldMarkedNoted, false);
		testRule("note==\"ala ma kota\"", ldMarkedNoted, true);
	}

	@Test
	public void testMethod() throws RuleException {
		testRule("method==abcd", ldClassMethod, false);
		testRule("method==myMethod", ldClassMethod, true);
	}

	@Test
	public void testClass() throws RuleException {
		testRule("CLASS==value", ldClassMethod, false);
		testRule("CLASS==a.b.C", ldClassMethod, true);
	}

	@Test
	public void testProperties() throws RuleException {
		testRule("PROP.key==value", ldWithProperties, true);
		testRule("PROP.key!=value", ldWithProperties, false);

		testRule("PROP.secondKey==value2", ldWithProperties, true);
		testRule("PROP.secondKey~=value", ldWithProperties, true);
		testRule("PROP.secondKey~=valueX", ldWithProperties, false);
		testRule("PROP.secondKey like .alu.2", ldWithProperties, true);

	}

	@Test
	public void testDate() throws RuleException {
		testRule("date>\"2001-01-02 22:22:22\"", ld_2011_12_01_120000, true);
		testRule("date<\"2001-01-02 22:22:22\"", ld_2011_12_01_120000, false);

		testRule("date>\"2021-01-02 22:22:22\"", ld_2011_12_01_120000, false);
		testRule("date<\"2021-01-02 22:22:22\"", ld_2011_12_01_120000, true);

		testRule("date>=\"2021-01-02 22:22:22\"", ld_2011_12_01_120000, false);
		testRule("date<=\"2021-01-02 22:22:22\"", ld_2011_12_01_120000, true);
	}

	@Test
	public void testTimesamp() throws RuleException {
		testRule("timestamp>\"2001-01-02 22:22:22\"", ld_2011_12_01_120000,
				true);
		testRule("timestamp<\"2001-01-02 22:22:22\"", ld_2011_12_01_120000,
				false);

		testRule("timestamp>\"2021-01-02 22:22:22\"", ld_2011_12_01_120000,
				false);
		testRule("timestamp<\"2021-01-02 22:22:22\"", ld_2011_12_01_120000,
				true);

		testRule("timestamp>=\"2021-01-02 22:22:22\"", ld_2011_12_01_120000,
				false);
		testRule("timestamp<=\"2021-01-02 22:22:22\"", ld_2011_12_01_120000,
				true);
	}

	@Test
	public void testMessage() throws RuleException {
		testRule("msg~=a", ldCurrentTimeLevelInfo, true);
		testRule("msg like .b", ldCurrentTimeLevelInfo, true);
		testRule("msg~=ab", ldCurrentTimeLevelInfo, true);
		testRule("msg~=abc", ldCurrentTimeLevelInfo, false);

		testRule("msg==ab", ldCurrentTimeLevelInfo, true);
		testRule("msg!=ab", ldCurrentTimeLevelInfo, false);
		testRule("msg!=abc", ldCurrentTimeLevelInfo, true);
		testRule("msg!=a", ldCurrentTimeLevelInfo, true);

		testRule("message==ab", ldCurrentTimeLevelInfo, true);
		testRule("message!=ab", ldCurrentTimeLevelInfo, false);
		testRule("message!=abc", ldCurrentTimeLevelInfo, true);
		testRule("message!=a", ldCurrentTimeLevelInfo, true);

	}

	@Test
	public void levelTest() throws RuleException {
		testRule("Level<WARNING", ldCurrentTimeLevelInfo, true);
		testRule("Level<SEVERE", ldCurrentTimeLevelInfo, true);
		testRule("Level<INFO", ldCurrentTimeLevelInfo, false);

		testRule("Level>WARNING", ldCurrentTimeLevelInfo, false);
		testRule("Level>SEVERE", ldCurrentTimeLevelInfo, false);
		testRule("Level>INFO", ldCurrentTimeLevelInfo, false);
		testRule("Level>FINE", ldCurrentTimeLevelInfo, true);

		testRule("Level<=INFO", ldCurrentTimeLevelInfo, true);
		testRule("Level<=WARNING", ldCurrentTimeLevelInfo, true);
		testRule("Level>=INFO", ldCurrentTimeLevelInfo, true);
		testRule("Level>=warning", ldCurrentTimeLevelInfo, false);

		testRule("Level==INFO", ldCurrentTimeLevelInfo, true);
		testRule("Level==warning", ldWarning, true);

		testRule("Level!=INFO", ldCurrentTimeLevelInfo, false);

	}

	@Test
	public void testInvalidMarked() throws RuleException {
		try {
			testRule("tralala==false", ldMarkedNoted, false);
			Assert.fail();
		} catch (RuleException e) {
			// s
		}

	}

	
	public void testRule(String expression, LogData ld, boolean expectedResult)
			throws RuleException {
		QueryAcceptCondition condition = new QueryAcceptCondition(expression);
		boolean evaluate = condition.accept(ld);
		assertEquals(expectedResult, evaluate);
	}

}
