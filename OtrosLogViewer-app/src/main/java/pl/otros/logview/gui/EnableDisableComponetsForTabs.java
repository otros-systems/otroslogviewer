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
package pl.otros.logview.gui;

import pl.otros.logview.api.LogViewPanelWrapper;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashSet;

public class EnableDisableComponetsForTabs implements ChangeListener {

  private final HashSet<Component> componets = new HashSet<>();
  private final JTabbedPane tabbedPane;

  public EnableDisableComponetsForTabs(JTabbedPane tabbedPane) {
    super();
    this.tabbedPane = tabbedPane;
  }

  @Override
  public void stateChanged(ChangeEvent arg0) {
    boolean enable = checkIfEnable();
    for (Component c : componets) {
      c.setEnabled(enable);
    }
  }

  public boolean checkIfEnable() {
    boolean result = false;
    if (tabbedPane.getTabCount() > 0 && tabbedPane.getSelectedComponent() instanceof LogViewPanelWrapper) {
      result = true;
    }

    return result;
  }

  public void addComponet(Component component) {
    componets.add(component);
  }

  public boolean removeComponent(Component component) {
    return componets.remove(component);
  }
}
