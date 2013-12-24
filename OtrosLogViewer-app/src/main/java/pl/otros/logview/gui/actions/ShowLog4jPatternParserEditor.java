/*
 * Copyright 2012 Krzysztof Otrebski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.otros.logview.gui.actions;

import pl.otros.logview.gui.Icons;
import pl.otros.logview.gui.Log4jPatternParserEditor;
import pl.otros.logview.gui.OtrosApplication;

import java.awt.event.ActionEvent;

public class ShowLog4jPatternParserEditor extends OtrosAction {

  private Log4jPatternParserEditor log4jEditor;

  public ShowLog4jPatternParserEditor(OtrosApplication otrosApplication) {
    super(otrosApplication);
    putValue(NAME, "Show Log4j pattern parser editor");
    putValue(SHORT_DESCRIPTION, "Show Log4j pattern parser editor.");
    putValue(SMALL_ICON, Icons.WRENCH);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (log4jEditor == null) {
      log4jEditor = new Log4jPatternParserEditor(getOtrosApplication());
    }
    getOtrosApplication().addClosableTab("Lo4j pattern parser editor", "Lo4j pattern parser editor", Icons.WRENCH, log4jEditor, true);
  }

}
