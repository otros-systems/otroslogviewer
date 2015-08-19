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
package pl.otros.logview.gui.util;

import javax.swing.*;

public abstract class DelayedSwingInvoke {

  protected long lastTextFieldEditTime = 0;
  protected int actionDelay = 1000;

  public DelayedSwingInvoke(int actionDelay) {
    this.actionDelay = actionDelay;
  }

  public DelayedSwingInvoke() {
    this(1000);
  }

  public void performAction() {
    lastTextFieldEditTime = System.currentTimeMillis();
    Timer timer = new Timer(actionDelay, e -> {
      if (System.currentTimeMillis() - lastTextFieldEditTime >= actionDelay) {
        performActionHook();
      }
    });
    timer.setRepeats(false);
    timer.start();
  }

  public void performActionNow() {
    lastTextFieldEditTime = System.currentTimeMillis();
    performActionHook();
  }

  protected abstract void performActionHook();

  public int getActionDelay() {
    return actionDelay;
  }

  public void setActionDelay(int actionDelay) {
    this.actionDelay = actionDelay;
  }

}
