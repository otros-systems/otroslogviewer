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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.LogPatternParserEditor;
import pl.otros.logview.gui.OtrosApplication;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

public class ShowLogPatternParserEditor extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShowLogPatternParserEditor.class.getName());
  private LogPatternParserEditor log4jEditor;
  private final String logPatternResourceName;

  public ShowLogPatternParserEditor(OtrosApplication otrosApplication,
                                    String logPatternResourceName,
                                    String actionName,
                                    String shortDescription,
                                    Icon icon) {
    super(otrosApplication);
    this.logPatternResourceName = logPatternResourceName;
    putValue(NAME, actionName);
    putValue(SHORT_DESCRIPTION, shortDescription);
    putValue(SMALL_ICON, icon);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (log4jEditor == null) {
      String logPatternText = "";
      ;
      try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(logPatternResourceName);) {
        logPatternText = IOUtils.toString(resourceAsStream);
      } catch (IOException ex) {
        LOGGER.error("Can't load default value of property editor");
      }
      log4jEditor = new LogPatternParserEditor(getOtrosApplication(), logPatternText);
    }
    getOtrosApplication().addClosableTab(getValue(NAME).toString(), getValue(SHORT_DESCRIPTION).toString(), (Icon) getValue(SMALL_ICON), log4jEditor, true);
  }

}
