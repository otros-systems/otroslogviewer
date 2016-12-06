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

import pl.otros.vfs.browser.i18n.Messages;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SftpUserAuthenticator extends UserPassUserAuthenticator {

  private JTextField sshKeyFileField;
  private static JFileChooser chooser;

  public SftpUserAuthenticator(AuthStore authStore, String url, FileSystemOptions fileSystemOptions) {
    super(authStore, url, fileSystemOptions);

  }

  @Override
  protected void getAuthenticationData(UserAuthenticationData authenticationData) {
    super.getAuthenticationData(authenticationData);
    authenticationData.setData(UserAuthenticationDataWrapper.SSH_KEY, sshKeyFileField.getText().trim().toCharArray());

    if (StringUtils.isNotBlank(sshKeyFileField.getText())) {
      try {
        SftpFileSystemConfigBuilder.getInstance().setIdentities(getFileSystemOptions(), new File[]{new File(sshKeyFileField.getText())});
        //TODO set user auth data file path
      } catch (FileSystemException e) {
        e.printStackTrace();
      }
    }

  }

  @Override
  protected JPanel getOptionsPanel() {
    if (sshKeyFileField == null) {
      sshKeyFileField = new JTextField(15);
    }
    if (chooser == null) {
      chooser = new JFileChooser();
    }
    JPanel panel = super.getOptionsPanel();
    panel.add(new JLabel(Messages.getMessage("authenticator.sshKeyFile")));

    panel.add(sshKeyFileField, "grow");
    JButton browseButton = new JButton(Messages.getMessage("authenticator.browse"));
    browseButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(Messages.getMessage("authenticator.selectSshKey"));
        int showOpenDialog = chooser.showOpenDialog(null);
        if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
          sshKeyFileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
      }
    });
    panel.add(browseButton,"wrap");
    panel.add(new JLabel(Messages.getMessage("authenticator.sshKeyFileDescription")),"span");

    return panel;
  }


  @Override
  protected void userSelectedHook(UserAuthenticationData userAuthenticationData) {
    if (userAuthenticationData != null) {
      char[] sshKeyPath = userAuthenticationData.getData(UserAuthenticationDataWrapper.SSH_KEY);
      String path = "";
      if (sshKeyPath != null && sshKeyPath.length > 0) {
        path = new String(sshKeyPath);
      }
      sshKeyFileField.setText(path);
    }
  }


}
