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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class CompositeAuthStore implements AuthStore {

  public AuthStore[] authStores;


  public CompositeAuthStore(AuthStore... authStores) {
    super();
    this.authStores = authStores;
  }

  @Override
  public void add(UserAuthenticationInfo auInfo, UserAuthenticationDataWrapper authenticationData) {
    for (AuthStore authStore : authStores) {
      authStore.add(auInfo, authenticationData);
    }

  }

  @Override
  public UserAuthenticationDataWrapper getUserAuthenticationData(UserAuthenticationInfo auInfo) {
    for (AuthStore authStore : authStores) {
      UserAuthenticationDataWrapper userAuthenticationData = authStore.getUserAuthenticationData(auInfo);
      if (userAuthenticationData != null) {
        return userAuthenticationData;
      }
    }
    return null;
  }

  @Override
  public Collection<UserAuthenticationDataWrapper> getUserAuthenticationDatas(String protocol, String host) {
    HashSet<UserAuthenticationDataWrapper> set = new HashSet<UserAuthenticationDataWrapper>();
    for (AuthStore authStore : authStores) {
      set.addAll(authStore.getUserAuthenticationDatas(protocol, host));
    }
    return set;
  }

  @Override
  public void remove(UserAuthenticationInfo authenticationInfo) {
    for (AuthStore authStore : authStores) {
      authStore.remove(authenticationInfo);
    }

  }

  @Override
  public Collection<UserAuthenticationInfo> getAll() {
    List<UserAuthenticationInfo> l = new ArrayList<UserAuthenticationInfo>();
    for (AuthStore authStore : authStores) {
      l.addAll(authStore.getAll());
    }
    return l;
  }

}
