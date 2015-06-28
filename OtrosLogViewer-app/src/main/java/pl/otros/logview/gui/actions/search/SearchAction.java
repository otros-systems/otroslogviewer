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
package pl.otros.logview.gui.actions.search;

import org.apache.commons.configuration.DataConfiguration;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.accept.query.QueryAcceptCondition;
import pl.otros.logview.accept.query.org.apache.log4j.rule.RuleException;
import pl.otros.logview.gui.*;
import pl.otros.logview.gui.actions.OtrosAction;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SearchAction extends OtrosAction {

	public enum SearchMode {
		STRING_CONTAINS("String contains search"), REGEX("Regex search"), QUERY("Query search");

		private final String name;

		private SearchMode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	private SearchMode searchMode = SearchMode.STRING_CONTAINS;
	private boolean markFound = false;
	private SearchEngine searchEngine;
	private MarkerColors markerColors = MarkerColors.Aqua;
	private final SearchDirection searchDirection;

	public SearchAction(OtrosApplication otrosApplication, SearchDirection searchDirection) {
		super(otrosApplication);
		this.searchDirection = searchDirection;
		if (searchDirection.equals(SearchDirection.FORWARD)) {
			this.putValue(Action.NAME, "Next");
			this.putValue(Action.SMALL_ICON, Icons.ARROW_DOWN);
		} else {
			this.putValue(Action.NAME, "Previous");
			this.putValue(Action.SMALL_ICON, Icons.ARROW_UP);
		}
		searchEngine = new SearchEngine();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		StatusObserver statusObserver = getOtrosApplication().getStatusObserver();
		String text = getOtrosApplication().getSearchField().getText().trim();
		if (text.trim().length() == 0) {
			statusObserver.updateStatus("No search criteria", StatusObserver.LEVEL_WARNING);
			return;
		}
		performSearch(text, searchDirection);
	}

	public void performSearch(String text, SearchDirection direction) {
		StatusObserver statusObserver = getOtrosApplication().getStatusObserver();
		JTabbedPane jTabbedPane = getOtrosApplication().getJTabbedPane();
		LogViewPanelWrapper lvPanel = (LogViewPanelWrapper) jTabbedPane.getSelectedComponent();
		if (lvPanel == null) {
			return;
		}
		JTable table = lvPanel.getLogViewPanel().getTable();

		NextRowProvider nextRowProvider = NextRowProviderFactory.getFilteredTableRow(table, direction);
		SearchContext context = new SearchContext();
		context.setDataTableModel(lvPanel.getDataTableModel());
		SearchMatcher searchMatcher = null;
		String confKey = null;
		if (SearchMode.STRING_CONTAINS.equals(searchMode)) {
			searchMatcher = new StringContainsSearchMatcher(text);
			confKey = ConfKeys.SEARCH_LAST_STRING;
		} else if (SearchMode.REGEX.equals(searchMode)) {
			try {
				searchMatcher = new RegexMatcher(text);
				confKey = ConfKeys.SEARCH_LAST_REGEX;
			} catch (Exception e) {
				statusObserver.updateStatus("Error in regular expression: " + e.getMessage(), StatusObserver.LEVEL_ERROR);
				return;
			}
		} else if (SearchMode.QUERY.equals(searchMode)) {
			QueryAcceptCondition acceptCondition;
			try {
				acceptCondition = new QueryAcceptCondition(text);
				searchMatcher = new AcceptConditionSearchMatcher(acceptCondition);
				confKey = ConfKeys.SEARCH_LAST_QUERY;
			} catch (RuleException e) {
				statusObserver.updateStatus("Wrong query rule: " + e.getMessage(), StatusObserver.LEVEL_ERROR);
				return;

			}
		}
		updateList(confKey, getOtrosApplication().getConfiguration(), text);
//		DefaultComboBoxModel model = (DefaultComboBoxModel) getOtrosApplication().getSearchField().getModel();
//		model.removeElement(text);
//		model.insertElementAt(text, 0);
//		model.setSelectedItem(text);
//		int maxCount = getOtrosApplication().getConfiguration().getInt(ConfKeys.SEARCH_LAST_COUNT, 30);
//		while (model.getSize() > maxCount) {
//			model.removeElementAt(model.getSize() - 1);
//		}

		context.setSearchMatcher(searchMatcher);
		SearchResult searchNext = searchEngine.searchNext(context, nextRowProvider);
		if (searchNext.isFound()) {
			int row = table.convertRowIndexToView(searchNext.getRow());
			Rectangle rect = table.getCellRect(row, 0, true);
			table.scrollRectToVisible(rect);
			table.clearSelection();
			table.setRowSelectionInterval(row, row);
			statusObserver.updateStatus(String.format("Found at row %d", row), StatusObserver.LEVEL_NORMAL);
			if (markFound) {
				lvPanel.getDataTableModel().markRows(markerColors, table.convertRowIndexToModel(row));
			}

			scrollToSearchResult(searchMatcher.getFoundTextFragments(lvPanel.getDataTableModel().getLogData(table.convertRowIndexToModel(row))), lvPanel
					.getLogViewPanel().getLogDetailTextArea());
		} else {
			statusObserver.updateStatus(String.format("\"%s\" not found", text), StatusObserver.LEVEL_WARNING);
		}
	}

	private void updateList(String configurationKey, DataConfiguration configuration, String text) {
		List<Object> list = configuration.getList(configurationKey);
		if (list.contains(text)) {
			list.remove(text);
		}
		list.add(0, text);
		if (list.size() > configuration.getInt(ConfKeys.SEARCH_LAST_COUNT, 30)) {
			list.remove(list.size() - 1);
		}
		configuration.setProperty(configurationKey, list);
	}

	private void scrollToSearchResult(ArrayList<String> toHighlight, JTextPane textPane) {
		if (toHighlight.size() == 0) {
			return;
		}
		try {
			StyledDocument logDetailsDocument = textPane.getStyledDocument();
			String text = logDetailsDocument.getText(0, logDetailsDocument.getLength());
			String string = toHighlight.get(0);
			textPane.setCaretPosition(Math.max(text.indexOf(string), 0));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public boolean isMarkFound() {
		return markFound;
	}

	public void setMarkFound(boolean markFound) {
		this.markFound = markFound;
	}

	public MarkerColors getMarkerColors() {
		return markerColors;
	}

	public void setMarkerColors(MarkerColors markerColors) {
		this.markerColors = markerColors;
	}

	public void setSearchMode(SearchMode searchMode) {
		this.searchMode = searchMode;
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}

}
