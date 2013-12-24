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

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SearchFieldKeyListener extends KeyAdapter {

  private SearchAction searchAction;
  private JTextField searchTextField;

  public SearchFieldKeyListener(SearchAction searchAction, JTextField searchTextField) {
    super();
    this.searchAction = searchAction;
    this.searchTextField = searchTextField;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == 10) {
			String text = searchTextField.getText().trim();
			if (text.length()==0){
				return;
			}
			if (0 == e.getModifiers()) {
        searchAction.performSearch(text, SearchDirection.FORWARD);
      } else if (KeyEvent.ALT_MASK == e.getModifiers()) {
        searchAction.performSearch(text, SearchDirection.REVERSE);
      }
    }
  }

}
