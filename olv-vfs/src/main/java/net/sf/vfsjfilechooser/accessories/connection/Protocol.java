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
package net.sf.vfsjfilechooser.accessories.connection;


/**
 * <p>
 * T1ODO Disable the SSL port option when not supported in the connection dialog
 * It's probably better/faster than removing the SSL member
 * For the file protocol no port will be associated, need to handle that case
 * using a -1 value in the connection dialog
 * </p>
 * Enumeration holding protocol constants
 *
 * @author Yves Zoundi<yveszoundi at users dot sf dot net>
 * @version 0.0.1
 */
public enum Protocol { // Protocol constants
  SMB("SMB", 445, "Connect to windows LAN or SAMBA"),
  SFTP("SFTP", 22, "Connect to a SSH server"),
  FTP("FTP", 21, "Connect to a FTP server"),
  WEBDAV("WEBDAV", 9800, "Connect to a WEBDAV server"),
  HTTP("HTTP", 80, "Connect to a HTTP server"),
  HTTPS("HTTPS", 443, "HTTP connection over SSL"),
  FILE("FILE", -1, "Local files");

  private final String name; // displayed name
  private final Integer port; // port number
  private final String description; // protocol description

  /**
   * Create a new protocol
   *
   * @param name The name of the protocol
   * @param port The port used by the protocol
   */
  Protocol(final String name, final int port, final String description) {
    this.name = name;
    this.port = port;
    this.description = description;
  }

  /**
   * Returns the protocol name
   *
   * @return the protocol name
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the protocol port number
   *
   * @return the protocol port number
   */
  public final int getPort() {
    return port;
  }

  /**
   * Returns the protocol description
   *
   * @return the protocol description
   */
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
