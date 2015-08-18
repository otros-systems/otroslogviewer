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
package pl.otros.logview.gui.actions.globalhotkeys;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class KeyboardTabSwitcher implements KeyEventPostProcessor {

  private final JTabbedPane pane;

  public KeyboardTabSwitcher(JTabbedPane pane) {
    super();
    this.pane = pane;
  }

  @Override
  public boolean postProcessKeyEvent(KeyEvent e) {
    if (e.getID() == KeyEvent.KEY_PRESSED && e.getModifiers() == InputEvent.CTRL_MASK) {
      if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT) {
        goPrevPane();
        return true;
      } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_RIGHT) {
        goNextPane();
        return true;
      }

    }
    return false;
  }

  public void goNextPane() {
    switchPane(1);
  }

  public void goPrevPane() {
    switchPane(-1);
  }

  private void switchPane(int indexChange) {
    if (pane.getTabCount() < 2) {
      return;
    }
    int selectedIndex = pane.getSelectedIndex();
    int nextPane = (pane.getTabCount() + selectedIndex + indexChange) % pane.getTabCount();
    pane.setSelectedIndex(nextPane);
  }
}
