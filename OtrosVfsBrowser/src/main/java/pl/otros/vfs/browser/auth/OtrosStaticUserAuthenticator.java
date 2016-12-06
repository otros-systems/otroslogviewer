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

import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticationData.Type;
import org.slf4j.Logger;


public class OtrosStaticUserAuthenticator implements OtrosUserAuthenticator {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OtrosStaticUserAuthenticator.class);

  private final UserAuthenticationDataWrapper userAuthenticationDataWrapper;
  private final UserAuthenticationData userAuthenticationData;


  public OtrosStaticUserAuthenticator(UserAuthenticationData userAuthenticationData) {
    this.userAuthenticationData = userAuthenticationData;
    userAuthenticationDataWrapper = new UserAuthenticationDataWrapper();

  }

  @Override
  public UserAuthenticationData requestAuthentication(Type[] arg0) {
    LOGGER.info("Received request for authentication");
    UserAuthenticationDataWrapper data = new UserAuthenticationDataWrapper();
    for (Type type : arg0) {
      data.setData(type, userAuthenticationData.getData(type));
      userAuthenticationDataWrapper.setData(type, userAuthenticationData.getData(type));
    }
    return data;
  }

  @Override
  public UserAuthenticationDataWrapper getLastUserAuthenticationData() {
    return userAuthenticationDataWrapper;
  }

  @Override
  public boolean isPasswordSave() {
    return false;
  }

}
