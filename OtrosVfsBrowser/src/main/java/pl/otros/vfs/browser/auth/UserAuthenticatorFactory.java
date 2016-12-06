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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAuthenticatorFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticatorFactory.class);

  public OtrosUserAuthenticator getUiUserAuthenticator(AuthStore authStore, AuthStore sessionAuthStore, String url,
                                                       FileSystemOptions fileSystemOptions) {
    LOGGER.info("Getting authenticator for {}", url);
    AbstractUiUserAuthenticator authenticator = null;
    if (url.startsWith("sftp://")) {
      authenticator = new SftpUserAuthenticator(authStore, url, fileSystemOptions);
    } else if (url.startsWith("smb://")) {
      authenticator = new SmbUserAuthenticator(authStore, url, fileSystemOptions);
    } else if (url.startsWith("ftp://")) {
      authenticator = new UserPassUserAuthenticator(authStore, url, fileSystemOptions);
    }
    UseCentralsFromSessionUserAuthenticator fromSessionUserAuthenticator = new UseCentralsFromSessionUserAuthenticator(authStore,
        sessionAuthStore,url,fileSystemOptions,authenticator);
    return fromSessionUserAuthenticator;

  }

}
