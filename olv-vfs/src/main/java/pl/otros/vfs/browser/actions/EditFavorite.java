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
import pl.otros.vfs.browser.favorit.Favorite;
import pl.otros.vfs.browser.i18n.Messages;
import pl.otros.vfs.browser.list.MutableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.event.ActionEvent;

import static pl.otros.vfs.browser.i18n.Messages.getMessage;

/**
 */
public class EditFavorite extends AbstractAction {

  private JList favoriteList;
  private MutableListModel<Favorite> listModel;


  public EditFavorite(JList favoriteList, MutableListModel<Favorite> listModel) {
    super(Messages.getMessage("editFavorites.actionName"), Icons.getInstance().getEditSignature());
    super.putValue(SHORT_DESCRIPTION, Messages.getMessage("editFavorites.tooltip"));
    this.favoriteList = favoriteList;
    this.listModel = listModel;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    if (favoriteList.getSelectedValue() != null && favoriteList.getSelectedValue() instanceof Favorite) {
      Favorite favorite = (Favorite) favoriteList.getSelectedValue();
      JPanel panel = new JPanel(new MigLayout());
      panel.add(new JLabel(Messages.getMessage("editFavorites.name")));

      final JTextField textFieldName = new JTextField(favorite.getName());
      textFieldName.setSelectionStart(0);
      textFieldName.setSelectionEnd(textFieldName.getText().length());
      panel.add(textFieldName, "wrap, growx");
      textFieldName.addAncestorListener(new AncestorListener() {


        @Override
        public void ancestorAdded(AncestorEvent ancestorEvent) {

          textFieldName.requestFocusInWindow();
        }

        @Override
        public void ancestorRemoved(AncestorEvent ancestorEvent) {
        }

        @Override
        public void ancestorMoved(AncestorEvent ancestorEvent) {
        }
      });
      panel.add(new JLabel(Messages.getMessage("editFavorites.url")));
      JTextField textFieldUrl = new JTextField(favorite.getUrl(), 50);
      panel.add(textFieldUrl, "wrap,growx");

      int response = JOptionPane.showConfirmDialog(SwingUtilities.getRoot(favoriteList), panel, getMessage("editFavorites.title"), JOptionPane.YES_NO_OPTION);
      if (response == JOptionPane.YES_OPTION) {
        favorite.setName(textFieldName.getText());
        favorite.setUrl(textFieldUrl.getText());
        listModel.change(favoriteList.getSelectedIndex(), favorite);
      }
    }

  }
}
