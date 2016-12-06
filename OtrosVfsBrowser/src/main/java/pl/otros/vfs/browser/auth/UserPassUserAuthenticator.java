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

package pl.otros.vfs.browser.auth;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import pl.otros.vfs.browser.i18n.Messages;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class UserPassUserAuthenticator extends AbstractUiUserAuthenticator {

  protected JXComboBox nameTf;
  protected JPasswordField passTx;


  public UserPassUserAuthenticator(AuthStore authStore, String url, FileSystemOptions fileSystemOptions) {
    super(authStore, url, fileSystemOptions);
  }

  @Override
  protected void getAuthenticationData(UserAuthenticationData authenticationData) {
    authenticationData.setData(UserAuthenticationData.USERNAME, nameTf.getSelectedItem().toString().toCharArray());
    authenticationData.setData(UserAuthenticationData.PASSWORD, passTx.getPassword());

  }

  @Override
  protected JPanel getOptionsPanel() {

    Collection<UserAuthenticationDataWrapper> userAuthenticationDatas = getAuthStore().getUserAuthenticationDatas(getVfsUriParser().getProtocol().getName(), getVfsUriParser().getHostname());
    String[] names = new String[userAuthenticationDatas.size()];
    int i = 0;
    for (UserAuthenticationData userAuthenticationData : userAuthenticationDatas) {
      names[i] = new String(userAuthenticationData.getData(UserAuthenticationData.USERNAME));
      i++;
    }

    JPanel panel = new JPanel(new MigLayout());
    String header = Messages.getMessage("authenticator.enterCredentialsForUrl", getUrl());
    panel.add(new JLabel(header), "growx,span");
    panel.add(new JLabel(Messages.getMessage("authenticator.username")));
    nameTf = new JXComboBox(names);
    nameTf.setEditable(true);
    nameTf.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        userSelected(nameTf.getSelectedItem().toString());
      }
    });


    AutoCompleteDecorator.decorate(nameTf);
    nameTf.addAncestorListener(new AncestorListener() {

      @Override
      public void ancestorRemoved(AncestorEvent event) {

      }

      @Override
      public void ancestorMoved(AncestorEvent event) {

      }

      @Override
      public void ancestorAdded(AncestorEvent event) {
        event.getComponent().requestFocusInWindow();
      }
    });
    panel.add(nameTf, "wrap, growx, span");
    panel.add(new JLabel(Messages.getMessage("authenticator.password")));
    passTx = new JPasswordField(15);
    passTx.setText(getVfsUriParser().getPassword());
    panel.add(passTx, "wrap, growx,span");

    if (StringUtils.isNotBlank(getVfsUriParser().getUsername())) {
      nameTf.setSelectedItem(getVfsUriParser().getUsername());
    }
    if (names.length > 0) {
      nameTf.setSelectedIndex(0);
    }
    return panel;
  }

  private void userSelected(String user) {
    UserAuthenticationData userAuthenticationData = getAuthStore().getUserAuthenticationData(new UserAuthenticationInfo(getVfsUriParser().getProtocol().getName(), getVfsUriParser().getHostname(), user));
    char[] passChars = new char[0];

    if (userAuthenticationData != null && userAuthenticationData.getData(UserAuthenticationData.PASSWORD) != null) {
      passChars = userAuthenticationData.getData(UserAuthenticationData.PASSWORD);
    }
    passTx.setText(new String(passChars));

    userSelectedHook(userAuthenticationData);
  }


  protected void userSelectedHook(UserAuthenticationData userAuthenticationData) {


  }

  /**
   * Override this method to be notified when user from authstore is selected
   *
   * @param authenticationData
   */
  protected void updateUserAuthenticationData(UserAuthenticationData authenticationData) {

  }


}
