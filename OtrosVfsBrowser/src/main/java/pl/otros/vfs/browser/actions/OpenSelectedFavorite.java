/*
 * Copyright 2013 Krzysztof Otrebski (otros.systems@gmail.com)
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

package pl.otros.vfs.browser.actions;

import pl.otros.vfs.browser.Icons;
import pl.otros.vfs.browser.VfsBrowser;
import pl.otros.vfs.browser.favorit.Favorite;
import pl.otros.vfs.browser.i18n.Messages;

import javax.swing.*;

/**
 */
public class OpenSelectedFavorite extends BaseNavigateAction {

  private JList favoriteList;

  public OpenSelectedFavorite(VfsBrowser vfsBrowser, JList favoriteList) {
    super(vfsBrowser, Messages.getMessage("general.openButtonText"), Icons.getInstance().getFolderOpen());
    this.favoriteList = favoriteList;
  }

  @Override
  protected void performLongOperation(CheckBeforeActionResult checkBeforeActionResult) {
    if (favoriteList.getSelectedValue() != null && favoriteList.getSelectedValue() instanceof Favorite) {
      Favorite favorite = (Favorite) favoriteList.getSelectedValue();
      browser.goToUrl(favorite.getUrl());
    }
  }

  @Override
  protected boolean canGoUrl() {
	  return favoriteList.getSelectedValue() != null && favoriteList.getSelectedValue() instanceof Favorite;
  }

@Override
protected boolean canExecuteDefaultAction() {
	return true;
}
}
