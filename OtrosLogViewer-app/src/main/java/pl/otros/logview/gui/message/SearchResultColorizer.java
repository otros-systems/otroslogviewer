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

import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.actions.search.SearchAction.SearchMode;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

public class SearchResultColorizer extends AbstractPluginableElement implements MessageColorizer {

	private Color color = Color.YELLOW;
	private String searchString = "";
	private SearchMode searchMode;

	public SearchResultColorizer() {
		super("Search result", "Mark search result");
	}

	@Override
	public int getApiVersion() {
		return MESSAGE_COLORIZER_VERSION_CURRENT;
	}

	@Override
	public boolean colorizingNeeded(String message) {
		if (StringUtils.isBlank(searchString)) {
			return false;
		}
		if (searchMode.equals(SearchMode.STRING_CONTAINS)) {
			return StringUtils.containsIgnoreCase(message, searchString);
		} else if (searchMode.equals(SearchMode.REGEX)) {
			try {
				Pattern p = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
				return (p.matcher(message).find());
			} catch (Exception e) {
				return false;
			}
		}
		//TODO QUERY MODE?

		return false;
	}

	@Override
	public Collection<MessageFragmentStyle> colorize(String textToColorize) throws BadLocationException {
		Collection<MessageFragmentStyle> list = new ArrayList<MessageFragmentStyle>();
		if (StringUtils.isEmpty(searchString)) {
			return list;
		}
		StyleContext sc = new StyleContext();
    Style searchStyle = sc.addStyle("searchResult", sc.getStyle(StyleContext.DEFAULT_STYLE));
		StyleConstants.setBackground(searchStyle, color);
		if (searchMode.equals(SearchMode.STRING_CONTAINS)) {
			list.addAll(colorizeString(textToColorize, searchStyle, searchString));
		} else if (searchMode.equals(SearchMode.REGEX)) {
			list.addAll(MessageColorizerUtils.colorizeRegex(searchStyle, textToColorize, Pattern.compile(searchString, Pattern.CASE_INSENSITIVE), 0));
		}
    for (MessageFragmentStyle style:list){
      style.setSearchResult(true);
    }
		return list;
	}

	private Collection<MessageFragmentStyle> colorizeString(String textToColorize, Style searchStyle, String toHighlight) {
		ArrayList<MessageFragmentStyle> list = new ArrayList<MessageFragmentStyle>();
		String text = textToColorize.toLowerCase();
		toHighlight = toHighlight.toLowerCase();
		int idx = 0;
		while ((idx = text.indexOf(toHighlight, idx)) > -1) {
			MessageFragmentStyle mfs = new MessageFragmentStyle(idx, toHighlight.length(), searchStyle, false);
			list.add(mfs);
			idx++;
		}
		return list;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(SearchMode searchMode) {
		this.searchMode = searchMode;
	}

}
