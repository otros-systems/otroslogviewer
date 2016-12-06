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

public class UserAuthenticationInfo {
  public final String protocol;
  public final String host;
  public final String user;

  public UserAuthenticationInfo(String protocol, String host, String user) {
    this.protocol = protocol;
    this.host = host;
    this.user = user;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
    result = prime * result + ((user == null) ? 0 : user.hashCode());
    return result;
  }


  public String getProtocol() {
    return protocol;
  }

  public String getHost() {
    return host;
  }

  public String getUser() {
    return user;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserAuthenticationInfo other = (UserAuthenticationInfo) obj;
    if (host == null) {
      if (other.host != null)
        return false;
    } else if (!host.equals(other.host))
      return false;
    if (protocol == null) {
      if (other.protocol != null)
        return false;
    } else if (!protocol.equals(other.protocol))
      return false;
    if (user == null) {
      if (other.user != null)
        return false;
    } else if (!user.equals(other.user))
      return false;
    return true;
  }


}