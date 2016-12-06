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

package pl.otros.vfs.browser.auth;

import javax.swing.*;
import java.awt.*;

public class DialogPasswordProvider implements PasswordProvider {
  @Override
  public char[] getPassword(String message) {
    JPanel jPanel = new JPanel(new GridLayout(2, 1));
    jPanel.add(new JLabel(message));
    JPasswordField comp = new JPasswordField(20);
    jPanel.add(comp);
    int i = JOptionPane.showConfirmDialog(null, jPanel, "Podaj haslo", JOptionPane.YES_NO_OPTION);
    if (i == JOptionPane.OK_OPTION) {
      return comp.getPassword();
    }
    return null;
  }


}
