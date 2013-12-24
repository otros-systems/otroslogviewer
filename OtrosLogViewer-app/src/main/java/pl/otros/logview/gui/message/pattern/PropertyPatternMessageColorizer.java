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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.message.MessageColorizer;
import pl.otros.logview.gui.message.MessageColorizerUtils;
import pl.otros.logview.gui.message.MessageFragmentStyle;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyPatternMessageColorizer implements MessageColorizer {

	public static final String PROP_TYPE = "type";
	public static final String PROP_VERSION = "version";
	public static final String PROP_NAME = "name";
	public static final String PROP_DESCRIPTION = "description";
	public static final String PROP_PATTERN = "pattern";
	public static final String PROP_PATTERN_CANON_EQ = "pattern.canon_eq";
	public static final String PROP_PATTERN_CASE_INSENSITIVE = "pattern.case_insensitive";
	public static final String PROP_PATTERN_COMMENTS = "pattern.comments";
	public static final String PROP_PATTERN_DOTALL = "pattern.dotall";
	public static final String PROP_PATTERN_LITERAL = "pattern.literal";
	public static final String PROP_PATTERN_MULTILINE = "pattern.multiline";
	public static final String PROP_PATTERN_UNICODE_CASE = "pattern.unicode_case";
	public static final String PROP_PATTERN_UNIX_LINES = "pattern.unix_lines";
	public static final String PROP_TEST_MESSAGE = "testMessage";

	private DataConfiguration configuration;
	private Pattern pattern;
	private Style style;
	private String name;
	private String description;
	private int groupCount;
	private String file;
	private String testMessage;
	private int version = 1;
	private PropertiesConfiguration propertiesConfiguration;

	public PropertyPatternMessageColorizer() {
	}

	public void init(InputStream in) throws ConfigurationException {
		propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);
		propertiesConfiguration.load(in, "UTF-8");
		configuration = new DataConfiguration(propertiesConfiguration);
		configuration.setDelimiterParsingDisabled(true);
		String pa = configuration.getString(PROP_PATTERN);
		int flags = 0;
		flags = flags | (configuration.getBoolean(PROP_PATTERN_CANON_EQ, false) ? Pattern.CANON_EQ : 0);
		flags = flags | (configuration.getBoolean(PROP_PATTERN_CASE_INSENSITIVE, false) ? Pattern.CASE_INSENSITIVE : 0);
		flags = flags | (configuration.getBoolean(PROP_PATTERN_COMMENTS, false) ? Pattern.COMMENTS : 0);
		flags = flags | (configuration.getBoolean(PROP_PATTERN_DOTALL, false) ? Pattern.DOTALL : 0);
		flags = flags | (configuration.getBoolean(PROP_PATTERN_LITERAL, false) ? Pattern.LITERAL : 0);
		flags = flags | (configuration.getBoolean(PROP_PATTERN_MULTILINE, false) ? Pattern.MULTILINE : 0);
		flags = flags | (configuration.getBoolean(PROP_PATTERN_UNICODE_CASE, false) ? Pattern.UNICODE_CASE : 0);
		flags = flags | (configuration.getBoolean(PROP_PATTERN_UNIX_LINES, false) ? Pattern.UNIX_LINES : 0);

		pattern = Pattern.compile(pa, flags);
		groupCount = countGroups(pattern);
		name = configuration.getString(PROP_NAME, "NAME NOT SET!");
		description = configuration.getString(PROP_DESCRIPTION, "DESCRIPTION NOT SET!");
		testMessage = configuration.getString(PROP_TEST_MESSAGE, "");
		version = configuration.getInt(PROP_VERSION, 1);
	}

	public void store(OutputStream out) throws ConfigurationException {
		propertiesConfiguration.save(out, "UTF-8");
	}

	@Override
	public boolean colorizingNeeded(String message) {
		Matcher matcher = pattern.matcher(message);
		boolean find = matcher.find();
		return find;
	}

	@Override
	public Collection<MessageFragmentStyle> colorize(String message) throws BadLocationException {
		Collection<MessageFragmentStyle> list = new ArrayList<MessageFragmentStyle>();
		StyleContext styleContext = new StyleContext();
		for (int i = 0; i <= groupCount; i++) {
			if (StyleProperties.isStyleForGroupDeclared(i, configuration)) {
				style = StyleProperties.getStyle(styleContext, configuration, "propStyle" + getName(), i);
				list.addAll(MessageColorizerUtils.colorizeRegex(style, message, pattern, i));
			}
		}

		return list;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	protected int countGroups(Pattern pattern) {
		int count = StringUtils.countMatches(pattern.pattern().replace("\\(", ""), "(");
		return count;
	}

	@Override
	public String getPluginableId() {
		return file;
	}

	@Override
	public int getApiVersion() {
		return MESSAGE_COLORIZER_VERSION_2;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getTestMessage() {
		return testMessage;
	}

	public void setTestMessage(String testMessage) {
		this.testMessage = testMessage;
		propertiesConfiguration.setProperty(PROP_TEST_MESSAGE, testMessage);
	}

	public void setName(String name) {
		this.name = name;
		propertiesConfiguration.setProperty(PROP_NAME, name);
	}

}
