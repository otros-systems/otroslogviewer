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
package pl.otros.logview.gui.actions;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.jdesktop.swingx.JXComboBox;
import pl.otros.logview.MarkerColors;
import pl.otros.logview.accept.query.QueryAcceptCondition;
import pl.otros.logview.accept.query.org.apache.log4j.rule.RuleException;
import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.LogDataTableModel;
import pl.otros.logview.gui.LogViewPanelWrapper;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.StatusObserver;
import pl.otros.logview.gui.actions.search.AcceptConditionSearchMatcher;
import pl.otros.logview.gui.actions.search.RegexMatcher;
import pl.otros.logview.gui.actions.search.SearchAction.SearchMode;
import pl.otros.logview.gui.actions.search.SearchMatcher;
import pl.otros.logview.gui.actions.search.StringContainsSearchMatcher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class MarkAllFoundAction extends OtrosAction implements ConfigurationListener, KeyListener {

  private MarkerColors markerColors = MarkerColors.Aqua;
  private SearchMode searchMode = SearchMode.STRING_CONTAINS;

  public MarkAllFoundAction(OtrosApplication otrosApplication) {
    super(otrosApplication);
    this.putValue(Action.NAME, "Mark all found");
    this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    this.putValue(SMALL_ICON, Icons.AUTOMATIC_MARKERS);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
		JTabbedPane jTabbedPane = getOtrosApplication().getJTabbedPane();
		LogViewPanelWrapper lvFrame = (LogViewPanelWrapper) jTabbedPane.getSelectedComponent();
    if (lvFrame == null) {
      return;
    }
    JTable table = lvFrame.getLogViewPanel().getTable();
    LogDataTableModel model = lvFrame.getDataTableModel();
		JXComboBox searchField = getOtrosApplication().getSearchField();
		StatusObserver statusObserver = getOtrosApplication().getStatusObserver();
		int marked = markAllFound(table, model, searchField.getSelectedItem().toString().trim(), markerColors);
		statusObserver.updateStatus(marked + " messages marked for string \"" + searchField.getSelectedItem().toString().trim() + "\"");
  }

  public int markAllFound(JTable table, LogDataTableModel dataTableModel, String string, MarkerColors markerColors) {
    string = string.trim().toLowerCase();
    if (string.length() == 0) {
      return 0;
    }

    SearchMatcher searchMatcher;
    if (SearchMode.STRING_CONTAINS.equals(searchMode)) {
      searchMatcher = new StringContainsSearchMatcher(string);
    } else if (SearchMode.REGEX.equals(searchMode)) {
      try {
        searchMatcher = new RegexMatcher(string);
      } catch (Exception e) {
        getOtrosApplication().getStatusObserver().updateStatus("Error in regular expression: " + e.getMessage(), StatusObserver.LEVEL_ERROR);
        return 0;
      }
    } else if (SearchMode.QUERY.equals(searchMode)){
      QueryAcceptCondition acceptCondition;
      try {
        acceptCondition = new QueryAcceptCondition(string);
        searchMatcher = new AcceptConditionSearchMatcher(acceptCondition);
      } catch (RuleException e) {
        getOtrosApplication().getStatusObserver().updateStatus("Wrong query rule: " + e.getMessage(), StatusObserver.LEVEL_ERROR);
        return 0;
      }
    }  else {
      getOtrosApplication().getStatusObserver().updateStatus("Unknown search mode", StatusObserver.LEVEL_ERROR);
      return 0;
    }

    ArrayList<Integer> toMark = new ArrayList<>();
    for (int i = 0; i < table.getRowCount(); i++) {
      int row = table.convertRowIndexToModel(i);
      if (searchMatcher.matches(dataTableModel.getLogData(row))) {
        toMark.add(row);
      }
    }
    if (toMark.size() > 0) {

      int[] rows = new int[toMark.size()];
      for (int i = 0; i < rows.length; i++) {
        rows[i] = toMark.get(i);

      }
      dataTableModel.markRows(markerColors, rows);
    }
    return toMark.size();
  }

  public void setSearchMode(SearchMode searchMode) {
    this.searchMode = searchMode;
  }

  public MarkerColors getMarkerColors() {
    return markerColors;
  }

  public void setMarkerColors(MarkerColors markerColors) {
    this.markerColors = markerColors;
  }

  @Override
  public void configurationChanged(ConfigurationEvent e) {
    if (e.getPropertyName() != null && e.getPropertyName().equalsIgnoreCase("gui.markColor")) {
      final Object propertyValue = e.getPropertyValue();
      if (propertyValue instanceof MarkerColors) {
        markerColors = (MarkerColors) propertyValue;
      } else if (propertyValue instanceof String) {
        String value = (String) propertyValue;
        markerColors = MarkerColors.fromString(value);
      }
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == 10 && KeyEvent.CTRL_MASK == e.getModifiers()) {
      actionPerformed(new ActionEvent(e.getSource(), e.getID(), ""));
    }

  }

  @Override
  public void keyReleased(KeyEvent e) {

  }

}
