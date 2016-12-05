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

package pl.otros.vfs.browser.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Set focus on component
 */
public class SetFocusOnAction extends AbstractAction {

  private final JComponent jComponent;

  public SetFocusOnAction(JComponent jComponent) {
    this.jComponent = jComponent;
  }

  public SetFocusOnAction(String name, JComponent jComponent) {
    super(name);
    this.jComponent = jComponent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
           jComponent.requestFocus();
  }
}
