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
package pl.otros.logview.gui.message;

import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static pl.otros.logview.gui.message.MessageColorizerUtils.increaseOffset;

public class StackTraceColorizer implements MessageColorizer {

	private static final String NANE = "Java stack trace";
	private static final Pattern exceptionLine = Pattern.compile("(\\s*at\\s+([\\w\\d\\.]*)\\.([\\w\\d\\$]+)\\.([\\d\\w<>]+)\\(([\\d\\w\\.\\u0020:]+)\\))");
	private static final int EXCEPTION_LINE_GROUP_PACKAGE = 2;
	private static final int EXCEPTION_LINE_GROUP_CLASS = 3;
	private static final int EXCEPTION_LINE_GROUP_METHOD = 4;
	private static final int EXCEPTION_LINE_GROUP_FILE = 5;
	private static final String DESCRIPTION = "Colorize java stack trace.";

	private Style styleStackTrace;
	private Style stylePackage;
	private Style styleClass;
	private Style styleMethod;
	private Style styleFile;

	private StackTraceFinder stackTraceFinder;

	public StackTraceColorizer() {
		stackTraceFinder = new StackTraceFinder();
		initStyles();
	}

	protected void initStyles() {
		StyleContext styleContext = new StyleContext();
		Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
		styleStackTrace = styleContext.addStyle("stackTrace", defaultStyle);
		StyleConstants.setBackground(styleStackTrace, new Color(255, 224, 193));
		StyleConstants.setForeground(styleStackTrace, Color.BLACK);
		StyleConstants.setFontFamily(styleStackTrace, "courier");

		stylePackage = styleContext.addStyle("stylePackage", styleStackTrace);

		styleClass = styleContext.addStyle("styleClass", stylePackage);
		StyleConstants.setForeground(styleClass, new Color(11, 143, 61));
		StyleConstants.setBold(styleClass, true);

		styleMethod = styleContext.addStyle("styleMethod", styleStackTrace);
		StyleConstants.setForeground(styleMethod, new Color(83, 112, 223));
		StyleConstants.setItalic(styleMethod, true);
		StyleConstants.setBold(styleMethod, true);

		styleFile = styleContext.addStyle("styleFile", styleStackTrace);
		StyleConstants.setForeground(styleFile, Color.BLACK);
		StyleConstants.setUnderline(styleFile, true);

	}

	@Override
	public boolean colorizingNeeded(String message) {

		return exceptionLine.matcher(message).find();
	}

	@Override
	public Collection<MessageFragmentStyle> colorize(String message) throws BadLocationException {
		Collection<MessageFragmentStyle> list = new ArrayList<MessageFragmentStyle>();
		SortedSet<SubText> foundStackTraces = stackTraceFinder.findStackTraces(message);

		for (SubText subText : foundStackTraces) {
			list.add(new MessageFragmentStyle(subText.getStart(), subText.getLength(), styleStackTrace, false));
			String subTextFragment = message.substring(subText.getStart(), subText.getEnd());
			Matcher matcher = exceptionLine.matcher(subTextFragment);
			while (matcher.find()) {
				int newOffset = subText.start;
				list.addAll(increaseOffset(MessageColorizerUtils.colorizeRegex(stylePackage, subTextFragment, exceptionLine, EXCEPTION_LINE_GROUP_PACKAGE),newOffset));
				list.addAll(increaseOffset(MessageColorizerUtils.colorizeRegex(styleClass, subTextFragment,  exceptionLine, EXCEPTION_LINE_GROUP_CLASS),newOffset));
				list.addAll(increaseOffset(MessageColorizerUtils.colorizeRegex(styleMethod, subTextFragment,  exceptionLine, EXCEPTION_LINE_GROUP_METHOD),newOffset));
				list.addAll(increaseOffset(MessageColorizerUtils.colorizeRegex(styleFile, subTextFragment,  exceptionLine, EXCEPTION_LINE_GROUP_FILE),newOffset));
			}
		}
		return list;
	}



	@Override
	public String getName() {
		return NANE;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getPluginableId() {
		return this.getClass().getName();
	}

	@Override
	public int getApiVersion() {
		return MESSAGE_COLORIZER_VERSION_CURRENT;
	}
}
