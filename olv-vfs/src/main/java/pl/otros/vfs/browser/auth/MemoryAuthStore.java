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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class MemoryAuthStore implements AuthStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(MemoryAuthStore.class);

  private HashMap<UserAuthenticationInfo, UserAuthenticationDataWrapper> map;

  public MemoryAuthStore() {
    map = new HashMap<UserAuthenticationInfo, UserAuthenticationDataWrapper>();
  }

  @Override
  public UserAuthenticationDataWrapper getUserAuthenticationData(UserAuthenticationInfo info) {
    return map.get(info);
  }

  @Override
  public Collection<UserAuthenticationDataWrapper> getUserAuthenticationDatas(String protocol, String host) {
    ArrayList<UserAuthenticationDataWrapper> list = new ArrayList<UserAuthenticationDataWrapper>();
    for (UserAuthenticationInfo key : map.keySet()) {
      if (StringUtils.equalsIgnoreCase(key.getProtocol(), protocol) && StringUtils.equalsIgnoreCase(key.getHost(), host)) {
        list.add(map.get(key));
      }
    }
    return list;
  }

  @Override
  public void add(UserAuthenticationInfo aInfo, UserAuthenticationDataWrapper authenticationData) {
    LOGGER.debug("Adding auth info {}://{}@{}", new Object[]{aInfo.getProtocol(), aInfo.getUser(), aInfo.getHost()});
    map.put(aInfo, authenticationData);
  }

  @Override
  public void remove(UserAuthenticationInfo authenticationInfo) {
    map.remove(authenticationInfo);
  }

  @Override
  public Collection<UserAuthenticationInfo> getAll() {
    return new ArrayList<UserAuthenticationInfo>(map.keySet());
  }


}
