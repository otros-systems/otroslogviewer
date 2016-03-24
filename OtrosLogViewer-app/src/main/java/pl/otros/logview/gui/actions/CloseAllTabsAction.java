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

import pl.otros.logview.api.OtrosApplication;

import java.awt.event.ActionEvent;

public class CloseAllTabsAction extends AbstractActionWithConfirmation {


  public CloseAllTabsAction(OtrosApplication otrosApplication) {
      super(otrosApplication);
    putValue(NAME, "Close all");
    putValue(SHORT_DESCRIPTION, "Close all open tabs");
  }

  @Override
  public void actionPerformedHook(ActionEvent e) {
        getOtrosApplication().getJTabbedPane().removeAll();
  }

  @Override
  public String getWarnningMessage() {
    return String.format("Do you really want to close %d tabs?", getOtrosApplication().getJTabbedPane().getTabCount());
  }

}
