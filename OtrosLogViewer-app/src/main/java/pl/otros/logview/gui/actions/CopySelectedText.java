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

import pl.otros.logview.api.OtrosAction;
import pl.otros.logview.api.Icons;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.gui.util.ClipboardUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 */
public class CopySelectedText extends OtrosAction {
  private final JTextPane logDetailTextArea;

  public CopySelectedText(OtrosApplication otrosApplication, JTextPane logDetailTextArea) {
    super(otrosApplication);
    this.logDetailTextArea = logDetailTextArea;
    putValue(NAME,"Copy selected text");
    putValue(SMALL_ICON, Icons.DOCUMENT_COPY);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String selectedText = logDetailTextArea.getSelectedText();
    if (logDetailTextArea.getSelectionStart()==logDetailTextArea.getSelectionEnd()){
      selectedText = logDetailTextArea.getText();
    }
    ClipboardUtil.copyToClipboard(selectedText);

  }
}
