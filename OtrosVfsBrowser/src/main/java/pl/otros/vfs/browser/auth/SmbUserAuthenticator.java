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

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.vfs.browser.i18n.Messages;

import javax.swing.*;

public class SmbUserAuthenticator extends UserPassUserAuthenticator {

  private static final Logger LOGGER = LoggerFactory.getLogger(SmbUserAuthenticator.class);

  private JTextField fieldTf;

  public SmbUserAuthenticator(AuthStore authStore, String url, FileSystemOptions fileSystemOptions) {
    super(authStore, url, fileSystemOptions);
  }

  @Override
  public UserAuthenticationData requestAuthentication(UserAuthenticationData.Type[] types) {
    LOGGER.debug("Requested for authentication");
    for (UserAuthenticationData.Type type : types) {
      LOGGER.debug("Requested for authentication: %s",type);
    }
    if (data == null) {
      return super.requestAuthentication(types);
    } else {
      return data;
    }
  }

  @Override
  protected void getAuthenticationData(UserAuthenticationData authenticationData) {
    super.getAuthenticationData(authenticationData);
    authenticationData.setData(UserAuthenticationData.DOMAIN, fieldTf.getText().toCharArray());
  }


  @Override
  protected void userSelectedHook(UserAuthenticationData userAuthenticationData) {
    char[] domain = new char[0];
    if (userAuthenticationData != null) {
      domain = userAuthenticationData.getData(UserAuthenticationData.DOMAIN);
    }
    fieldTf.setText(new String(domain));

  }

  @Override
  protected JPanel getOptionsPanel() {
    JPanel panel = super.getOptionsPanel();
    panel.add(new JLabel(Messages.getMessage("authenticator.domain")));
    fieldTf = new JTextField(15);
    panel.add(fieldTf, "grow");
    return panel;
  }

}
