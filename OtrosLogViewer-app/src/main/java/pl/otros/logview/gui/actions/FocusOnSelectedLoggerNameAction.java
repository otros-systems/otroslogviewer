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
import pl.otros.logview.api.gui.Icons;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.filter.LoggerNameFilter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FocusOnSelectedLoggerNameAction extends FocusOnThisAbstractAction<LoggerNameFilter> {

  /**
   *
   */
  private static final long serialVersionUID = 3032705743589544542L;

  public FocusOnSelectedLoggerNameAction(LoggerNameFilter filter, JCheckBox filterEnableCheckBox, OtrosApplication otrosApplication) {
    super(filter, filterEnableCheckBox, otrosApplication);
    this.putValue(NAME, "Focus on selected Logger");
    this.putValue(SHORT_DESCRIPTION, "Filter events, pass only events with selected logger name");
    this.putValue(SMALL_ICON, Icons.ARROW_BRANCH_270);
  }

  @Override
  public void action(ActionEvent e, LoggerNameFilter filter, LogData... selectedLogData) {
    String loggerName = selectedLogData[0].getLoggerName();
    filter.setLoggerNameFilterAndGuiChange(loggerName);
  }

}
