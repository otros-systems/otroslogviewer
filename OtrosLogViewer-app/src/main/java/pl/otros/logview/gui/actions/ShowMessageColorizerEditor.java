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
import pl.otros.logview.gui.message.editor.MessageColorizerBrowser;

import java.awt.event.ActionEvent;

public class ShowMessageColorizerEditor extends OtrosAction {

  private MessageColorizerBrowser mcEditor;
  private String messageText = null;

  public ShowMessageColorizerEditor(OtrosApplication otrosApplication) {
    super(otrosApplication);
    putValue(NAME, "Show message colorizer editor");
    putValue(SHORT_DESCRIPTION, "Show message colorizer editor. You can edit or create message colorizer.");
    putValue(SMALL_ICON, Icons.MESSAGE_COLORIZER);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
//		StatusObserver statusObserver = getOtrosApplication().getStatusObserver();
//		PluginableElementsContainer<MessageColorizer> container = getOtrosApplication().getAllPluginables().getMessageColorizers();
//		JTabbedPane tabbedPane = getOtrosApplication().getJTabbedPane();
		if (mcEditor == null) {
      mcEditor = new MessageColorizerBrowser( getOtrosApplication());
		}

    getOtrosApplication().addClosableTab("MessageColorizer editor","MessageColorizer editor",Icons.MESSAGE_COLORIZER,mcEditor,true);
  }

  public String getMessageText() {
    return messageText;
  }

  public void setMessageText(String messageText) {
    this.messageText = messageText;
  }

}
