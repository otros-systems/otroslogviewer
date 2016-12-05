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
package net.sf.vfsjfilechooser.utils;

import net.sf.vfsjfilechooser.accessories.connection.Protocol;

/**
 * VFSURIParser class for bookmarks URIs
 *
 * @author Yves Zoundi <yveszoundi at users dot sf dot net>
 * @author Stan Love
 * @version 0.0.6
 */
public final class VFSURIParser {
  private static final char PATH_SEPARATOR = '/';
  private String username;
  private String password;
  private String path;
  private String hostname;
  private String portnumber;
  private Protocol protocol;

  /**
   * Create a new instance of <CODE>VFSURIParser</CODE>
   *
   * @param fileURI The VFS file URI to parse
   */
  public VFSURIParser(final String fileURI) {
    this(fileURI, true);
  }

  public VFSURIParser(final String fileURI, boolean assignDefaultPort) {
    if (fileURI == null) {
      throw new NullPointerException("file URI is null");
    }

    VFSURIValidator v = new VFSURIValidator();
    boolean valid = v.isValid(fileURI);
    if (valid) {
      hostname = v.getHostname();
      username = v.getUser();
      password = v.getPassword();
      path = v.getFile();
      portnumber = v.getPort();
      String p = v.getProtocol();

      //fix up parsing results
      protocol = Protocol.valueOf(p.toUpperCase());
      if ((portnumber == null) && (!p.equalsIgnoreCase("file"))) {
        portnumber = String.valueOf(protocol.getPort());
      }
      if (path == null) {
        path = String.valueOf(PATH_SEPARATOR);
      }
    } else {
      hostname = null;
      username = null;
      password = null;
      path = fileURI;
      portnumber = null;
      protocol = null;
    }

  }

  /**
   * Returns the VFS hostname
   *
   * @return the VFS hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * Returns the VFS password
   *
   * @return the VFS password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Returns the VFS path
   *
   * @return the VFS path
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the VFS port number
   *
   * @return the VFS port number
   */
  public String getPortnumber() {
    return portnumber;
  }

  /**
   * Returns the VFS protocol
   *
   * @return the VFS protocol
   */
  public Protocol getProtocol() {
    return protocol;
  }

  /**
   * Returns the VFS username
   *
   * @return the VFS username
   */
  public String getUsername() {
    return username;
  }
}
