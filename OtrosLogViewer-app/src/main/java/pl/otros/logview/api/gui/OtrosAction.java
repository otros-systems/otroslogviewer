/*
 * Copyright 2012 Krzysztof Otrebski
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

package pl.otros.logview.api.gui;

import pl.otros.logview.api.OtrosApplication;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * User: Krzysztof Otrebski
 * Date: 3/29/12
 * Time: 6:55 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OtrosAction extends AbstractAction {
  private OtrosApplication otrosApplication;

  protected OtrosAction(OtrosApplication otrosApplication) {
    this.otrosApplication = otrosApplication;
  }

  protected OtrosAction(String name, OtrosApplication otrosApplication) {
    super(name);
    this.otrosApplication = otrosApplication;

  }

  protected OtrosAction(String name, Icon icon, OtrosApplication otrosApplication) {
    super(name, icon);
    this.otrosApplication = otrosApplication;
  }

  public OtrosApplication getOtrosApplication() {
    return otrosApplication;
  }

  public void setOtrosApplication(OtrosApplication otrosApplication) {
    this.otrosApplication = otrosApplication;
  }

  @Override
  public final void actionPerformed(ActionEvent e) {
    getOtrosApplication().getServices().getStatsService().actionExecuted(this);
    actionPerformedHook(e);
  }

  public Optional<String> actionModeForStats(){
    return Optional.empty();
  }

  protected abstract void actionPerformedHook(ActionEvent e);
}
