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

import pl.otros.logview.api.AcceptCondition;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.StatusObserver;
import pl.otros.logview.api.gui.HasIcon;
import pl.otros.logview.api.gui.LogDataTableModel;
import pl.otros.logview.api.gui.OtrosAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveByAcceptanceCriteria extends OtrosAction {

  private final AcceptCondition acceptCondition;

  public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication) {
    this(acceptCondition, otrosApplication, (Icon) null);
  }

  public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication, final Icon smallIcon) {
    super(otrosApplication);
    this.acceptCondition = acceptCondition;
    putValue(NAME, acceptCondition.getName());
    putValue(SHORT_DESCRIPTION, acceptCondition.getDescription());
    if (smallIcon == null && acceptCondition instanceof HasIcon) {
      putValue(SMALL_ICON, ((HasIcon) acceptCondition).getIcon());
    } else {
      putValue(SMALL_ICON, smallIcon);
    }
  }

  public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication, String name, Icon icon) {
    this(acceptCondition, otrosApplication, icon);
    putValue(NAME, name);
  }

  public RemoveByAcceptanceCriteria(AcceptCondition acceptCondition, OtrosApplication otrosApplication, String name) {
    this(acceptCondition, otrosApplication, name, null);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    StatusObserver observer = getOtrosApplication().getStatusObserver();
    LogDataTableModel dataTableModel = getOtrosApplication().getSelectedPaneLogDataTableModel();
    int removeRows = dataTableModel.removeRows(acceptCondition);
    if (observer != null) {
      observer.updateStatus(String.format("Removed %d rows using \"%s\"", removeRows, acceptCondition.getName()));
    }

  }

}
