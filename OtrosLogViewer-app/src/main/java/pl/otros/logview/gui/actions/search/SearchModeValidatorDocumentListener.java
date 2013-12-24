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

import pl.otros.logview.accept.query.QueryAcceptCondition;
import pl.otros.logview.gui.StatusObserver;
import pl.otros.logview.gui.actions.search.SearchAction.SearchMode;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

public class SearchModeValidatorDocumentListener implements DocumentListener {

  protected static final int EDIT_FILTER_ACTION_DELAY = 1000;
  protected long lastTextFieldEditTime = 0;
  private JTextField textField;
  protected Color editingColor = new Color(255, 255, 143);
  protected Color normalColor = null;
  protected Color errorColor = Color.RED;
  private StatusObserver statusObserver;
  private boolean enable = true;
  private SearchMode searchMode;

  public SearchModeValidatorDocumentListener(JTextField textField, StatusObserver observer, SearchMode searchMode) {
    super();
    this.textField = textField;
    normalColor = textField.getBackground();
    statusObserver = observer;
    this.searchMode = searchMode;
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    invokeDocumentChange();
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    invokeDocumentChange();

  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    invokeDocumentChange();

  }

  protected void invokeDocumentChange() {
    lastTextFieldEditTime = System.currentTimeMillis();
    if (enable) {
      textField.setBackground(editingColor);
      Timer timer = new Timer(EDIT_FILTER_ACTION_DELAY, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          if (enable && System.currentTimeMillis() - lastTextFieldEditTime >= EDIT_FILTER_ACTION_DELAY) {
            if (SearchMode.REGEX.equals(searchMode)) {
              try {
                Pattern.compile(textField.getText().trim());
                textField.setBackground(normalColor);
              } catch (Exception e1) {
                textField.setBackground(errorColor);
                statusObserver.updateStatus(String.format("Error in regular expression: %s", e1.getMessage()), StatusObserver.LEVEL_ERROR);
              }
            } else if (SearchMode.QUERY.equals(searchMode)) {
              try {
                new QueryAcceptCondition(textField.getText().trim());
                textField.setBackground(normalColor);
              } catch (Exception e1) {
                textField.setBackground(errorColor);
                statusObserver.updateStatus(String.format("Error in validating query: %s", e1.getMessage()), StatusObserver.LEVEL_ERROR);
              }
            }
          }
        }
      });
      timer.setRepeats(false);
      timer.start();
    }
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
    if (!enable) {
      textField.setBackground(normalColor);
    } else if (enable && textField.getText().length() > 0) {
      invokeDocumentChange();
    }
  }

  public SearchMode getSearchMode() {
    return searchMode;
  }

  public void setSearchMode(SearchMode searchMode) {
    this.searchMode = searchMode;
  }

}
