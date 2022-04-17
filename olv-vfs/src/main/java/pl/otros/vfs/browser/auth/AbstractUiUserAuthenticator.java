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

import com.google.common.base.Throwables;
import pl.otros.vfs.browser.i18n.Messages;
import net.sf.vfsjfilechooser.utils.VFSURIParser;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticationData.Type;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public abstract class AbstractUiUserAuthenticator implements OtrosUserAuthenticator {


  private String url;
  private VFSURIParser vfsUriParser;
  private AuthStore authStore;
  private final FileSystemOptions fileSystemOptions;
  protected UserAuthenticationDataWrapper data;
  private JCheckBox saveCredentialsCheckBox;


  public AbstractUiUserAuthenticator(AuthStore authStore, String url, FileSystemOptions fileSystemOptions) {
    this.authStore = authStore;
    this.url = url;
    this.fileSystemOptions = fileSystemOptions;
    vfsUriParser = new VFSURIParser(url);



  }


  @Override
  public UserAuthenticationData requestAuthentication(Type[] types) {
    try {
      Runnable doRun = new Runnable() {
        @Override
        public void run() {
          if (saveCredentialsCheckBox == null) {
            saveCredentialsCheckBox = new JCheckBox(Messages.getMessage("authenticator.savePassword"), true);
          }
          JPanel authOptionPanel = getOptionsPanel();

          JPanel panel = new JPanel(new BorderLayout());
          panel.add(authOptionPanel);
          panel.add(saveCredentialsCheckBox, BorderLayout.SOUTH);

          String[] options = {Messages.getMessage("general.okButtonText"), Messages.getMessage("general.cancelButtonText")};
          int showConfirmDialog = JOptionPane.showOptionDialog(null, panel, Messages.getMessage("authenticator.enterCredentials"),
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
          if (showConfirmDialog != JOptionPane.OK_OPTION) {
            throw new AuthorisationCancelledException("Authorization cancelled by user");
          }

          data = new UserAuthenticationDataWrapper();
          getAuthenticationData(data);
        }
      };
      if (SwingUtilities.isEventDispatchThread()) {
        doRun.run();
      } else {
        SwingUtilities.invokeAndWait(doRun);
      }
    } catch (Exception e) {
      if (Throwables.getRootCause(e) instanceof AuthorisationCancelledException) {
        throw (AuthorisationCancelledException) Throwables.getRootCause(e);
      }
    }


    return data;
  }


  @Override
  public UserAuthenticationDataWrapper getLastUserAuthenticationData() {
    return data;
  }


  @Override
  public boolean isPasswordSave() {
    return saveCredentialsCheckBox.isSelected();
  }

  protected abstract void getAuthenticationData(UserAuthenticationData authenticationData);

  protected abstract JPanel getOptionsPanel();

  protected String getUrl() {
    return url;
  }


  protected VFSURIParser getVfsUriParser() {
    return vfsUriParser;
  }


  protected AuthStore getAuthStore() {
    return authStore;
  }


  protected FileSystemOptions getFileSystemOptions() {
    return fileSystemOptions;
  }

}
