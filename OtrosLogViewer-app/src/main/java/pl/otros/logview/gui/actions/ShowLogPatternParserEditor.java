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
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.gui.OtrosAction;
import pl.otros.logview.gui.editor.LogPatternParserEditorBase;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class ShowLogPatternParserEditor extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShowLogPatternParserEditor.class.getName());
  private LogPatternParserEditorBase log4jEditor;
  private final Supplier<LogPatternParserEditorBase> viewSupplier;
  private String logPatternText = "";

  public ShowLogPatternParserEditor(OtrosApplication otrosApplication,
                                    String logPatternResourceName,
                                    String actionName,
                                    String shortDescription,
                                    Icon icon, Supplier<LogPatternParserEditorBase> viewSupplier) {
    super(otrosApplication);
    this.viewSupplier = viewSupplier;
    putValue(NAME, actionName);
    putValue(SHORT_DESCRIPTION, shortDescription);
    putValue(SMALL_ICON, icon);
    logPatternText = loadDefaultText(logPatternResourceName);

  }

  @Override
  protected void actionPerformedHook(ActionEvent e) {
    if (log4jEditor == null) {
      log4jEditor = viewSupplier.get();
      log4jEditor.setLogPattern(logPatternText);
    }
    getOtrosApplication().addClosableTab(getValue(NAME).toString(), getValue(SHORT_DESCRIPTION).toString(), (Icon) getValue(SMALL_ICON), log4jEditor, true);
  }

  private String loadDefaultText(String resources) {
    try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(resources)) {
      logPatternText = IOUtils.toString(resourceAsStream);
    } catch (IOException ex) {
      LOGGER.error("Can't load default value of property editor");
    }
    return logPatternText;
  }

}
