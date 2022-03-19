/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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
 */

package pl.otros.vfs.browser.list;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * When component gained focus is list without selection, first element is selected.
 */
public class SelectFirstElementFocusAdapter extends FocusAdapter {
  @Override
  public void focusGained(FocusEvent e) {
    if (e.getSource() instanceof JList) {
      JList list = (JList) e.getSource();
      if (list.getSelectedIndex() < 0 && list.getModel().getSize() > 0) {
        list.setSelectedIndex(0);
      }
    }
  }
}
