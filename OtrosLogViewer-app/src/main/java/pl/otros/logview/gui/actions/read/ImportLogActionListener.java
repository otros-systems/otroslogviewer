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
package pl.otros.logview.gui.actions.read;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.OtrosApplication;
import pl.otros.logview.gui.actions.OtrosAction;
import pl.otros.vfs.browser.JOtrosVfsBrowserDialog;
import pl.otros.vfs.browser.SelectionMode;

import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class ImportLogActionListener extends OtrosAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImportLogActionListener.class.getName());
  private final String title;

  public ImportLogActionListener(OtrosApplication otrosApplication, String title) {
    super(otrosApplication);
    this.title = title;
    putValue(NAME, title);

  }

  public synchronized void actionPerformed(ActionEvent e) {
    JOtrosVfsBrowserDialog chooser = getOtrosApplication().getOtrosVfsBrowserDialog();
    initFileChooser(chooser);
    JOtrosVfsBrowserDialog.ReturnValue result = chooser.showOpenDialog((Component) e.getSource(), title);
    if (result != JOtrosVfsBrowserDialog.ReturnValue.Approve) {
      LOGGER.debug("User cancel opening log");
      return;
    }
    openSelectedFiles(chooser);
  }


  private void openSelectedFiles(JOtrosVfsBrowserDialog chooser) {
    final FileObject[] files = chooser.getSelectedFiles();
    for (final FileObject file : files) {
      LOGGER.debug("WIll open {}", file);
      new LogFileInNewTabOpener(getLogImporterProvider(), getOtrosApplication()).open(file);
    }
  }

  private void initFileChooser(JOtrosVfsBrowserDialog chooser) {
    LOGGER.info("Initializing JOtrosVfsBrowserDialog");
    chooser.setSelectionMode(SelectionMode.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
  }

  protected abstract LogImporterProvider getLogImporterProvider();

}
