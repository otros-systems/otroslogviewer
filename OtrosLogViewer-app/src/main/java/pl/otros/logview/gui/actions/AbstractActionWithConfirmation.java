/*******************************************************************************
 * Copyright 2011 Krzysztof Otrebski
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package pl.otros.logview.gui.actions;

import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractActionWithConfirmation extends OtrosAction {

  private static boolean doNotAskAgain = false;

  public AbstractActionWithConfirmation(OtrosApplication otrosApplication) {
    super(otrosApplication);
  }

  @Override
  protected final void actionPerformedHook(ActionEvent e) {
    boolean confirm = true;
    if (!doNotAskAgain) {
      JPanel component = new JPanel(new BorderLayout());
      component.add(new JLabel(getWarnningMessage()));
      JCheckBox jCheckBox = new JCheckBox("Do not ask again!");
      component.add(jCheckBox, BorderLayout.SOUTH);
      int showConfirmDialog = JOptionPane.showConfirmDialog((Component) e.getSource(), component, "Confirm", JOptionPane.YES_NO_OPTION);
      confirm = showConfirmDialog == JOptionPane.YES_OPTION;
      doNotAskAgain = jCheckBox.isSelected();
    }
    if (confirm) {
      actionPerformedHook(e);
    }
  }

  public abstract void actionWithConfirmationPerformedHook(ActionEvent e);

  public abstract String getWarnningMessage();
}
