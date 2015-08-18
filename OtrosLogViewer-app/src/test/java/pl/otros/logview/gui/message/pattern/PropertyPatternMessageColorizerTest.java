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
package pl.otros.logview.gui.message.pattern;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.apache.commons.configuration.ConfigurationException;
import pl.otros.logview.gui.message.MessageFragmentStyle;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class PropertyPatternMessageColorizerTest {

	Properties p = new Properties();
	PropertyPatternMessageColorizer colorizer;

	@BeforeMethod
	public void setUp() throws ConfigurationException, IOException {
		p.put(PropertyPatternMessageColorizer.PROP_NAME, "Test");
		p.put(PropertyPatternMessageColorizer.PROP_DESCRIPTION, "D");
		p.put(PropertyPatternMessageColorizer.PROP_PATTERN, "a(\\d+\\(a\\))a");
		p.put("foreground", "#FFFF00");
		p.put("background", "#00FFFF");
		p.put("font.bold", "false");
		p.put("font.italic", "true");
		p.put("foreground.1", "#FF0000");
		p.put("background.1", "#0000FF");
		p.put("font.bold.1", "true");
		p.put("font.italic.1", "false");

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		p.store(bout, "");
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());

		colorizer = new PropertyPatternMessageColorizer();
		colorizer.init(bin);
	}

	@Test
	public void testColorizingNeeded() {
		assertTrue(colorizer.colorizingNeeded("a3(a)a"));
	}

	@Test
	public void testGetName() {
		assertEquals("Test", colorizer.getName());
	}

	@Test
	public void testGetDescription() {
		assertEquals("D", colorizer.getDescription());
	}

	@Test
	public void testCountGroups() {
		assertEquals(0, colorizer.countGroups(Pattern.compile("")));
		assertEquals(1, colorizer.countGroups(Pattern.compile("a(a)")));
		assertEquals(3, colorizer.countGroups(Pattern.compile("(a(a)(b))")));
		assertEquals(0, colorizer.countGroups(Pattern.compile("\\(asdf")));
		assertEquals(1, colorizer.countGroups(Pattern.compile("aaa\\(ffds(a)fsfd\\)\\(\\)")));
	}

	@Test
	public void testColorize() throws BadLocationException {
		String message = "a55(a)a";
		Collection<MessageFragmentStyle> colorize = colorizer.colorize(message);
		assertEquals(2, colorize.size());
		ArrayList<MessageFragmentStyle> list = new ArrayList<>(colorize);
		MessageFragmentStyle messageFragmentStyle = list.get(0);
		MessageFragmentStyle messageFragmentStyle1 = list.get(1);

		assertEquals(messageFragmentStyle.isReplace(), false);
		assertEquals(messageFragmentStyle.getOffset(), 0);
		assertEquals(messageFragmentStyle.getLength(), message.length());
		Style style = messageFragmentStyle.getStyle();
		assertEquals(StyleConstants.getBackground(style), new Color(0, 255, 255));
		assertEquals(StyleConstants.getForeground(style), new Color(255, 255, 0));
		assertEquals((Boolean)StyleConstants.isBold(style), Boolean.FALSE);
		assertEquals((Boolean)StyleConstants.isItalic(style), Boolean.TRUE);

		assertEquals(messageFragmentStyle1.isReplace(), false);
		assertEquals(messageFragmentStyle1.getOffset(), 1);
		assertEquals(messageFragmentStyle1.getLength(), 5);
		Style style1 = messageFragmentStyle1.getStyle();
		assertEquals(StyleConstants.getBackground(style1), new Color(0, 0, 255));
		assertEquals(StyleConstants.getForeground(style1), new Color(255, 0, 0));
		assertEquals((Boolean)StyleConstants.isBold(style1), Boolean.TRUE);
		assertEquals((Boolean)StyleConstants.isItalic(style1), Boolean.FALSE);


	}
}
