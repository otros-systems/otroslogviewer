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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VFS URIs validator
 *
 * @author Stan Love <stanlove at users.sourceforge.net>
 * @version 0.0.4
 */
class VFSURIValidator {

  private String local_uri, local_protocol, local_user, local_pass;
  private String local_hostname, local_port, local_file;
  private boolean valid = false;

  public static VFSURIValidator validate(String url) {
    return new VFSURIValidator(url);
    }

  public String getUri() {
    if ("".equals(local_uri))
      local_uri = null;
    return local_uri;
  }

  public String getProtocol() {
    if ((local_protocol != null) && (local_protocol.equals("")))
      local_protocol = null;
    return local_protocol;
  }

  public String getUser() {
    if ((local_user != null) && (local_user.equals("")))
      local_user = null;
    return local_user;
  }

  public String getPassword() {
    if ((local_pass != null) && (local_pass.equals("")))
      local_pass = null;
    return local_pass;
  }

  public String getHostname() {
    if ((local_hostname != null) && (local_hostname.equals("")))
      local_hostname = null;
    return local_hostname;
  }

  public String getPort() {
    if (local_port == null) {
      return null;
    }
    if (local_port.startsWith(":")) {
      local_port = local_port.substring(1);
    }
    if (local_port.equals(""))
      local_port = null;
    return local_port;
  }

  public String getFile() {
    if ((local_file != null) && (local_file.equals("")))
      local_file = null;
    return local_file;
  }

  private VFSURIValidator(String _uri) {
    boolean ret = false;
    boolean ends_with_slash = false;

    String protocol = null;
    String user_pass = null;
    String hostname = null;
    String port = null;
    String bad_port = null;
    String drive;
    String file = null;

    /*
      System.out.println();
      System.out.println();
      System.out.println();
       */

    local_uri = null;
    local_protocol = null;
    local_user = null;
    local_pass = null;
    local_hostname = null;
    local_port = null;
    local_file = null;
    //file://(drive_letter:)/
    //file://(drive_letter:)/file_path
    //Pattern p_file1 = Pattern.compile("(file|FILE)://([a-z][ 	]*:)*?(/.*)");
    Pattern p_file1 = Pattern
        .compile("(file|FILE)://(/*)([a-zA-Z][ 	]*:)*(.*)");
    Matcher m_file1 = p_file1.matcher(_uri);

    if (m_file1.matches()) {
      //System.out.println("file matcher");
      protocol = m_file1.group(1);
      String path_start = m_file1.group(2);
      drive = m_file1.group(3);
      file = m_file1.group(4);

      /*
         System.out.println("uri="+_uri+"=");
         System.out.println("drive="+drive+"=");
         System.out.println("file="+file+"=");
         System.out.println("path_start="+path_start+"=");
         */
      local_uri = _uri;
      local_protocol = protocol;
      local_user = null;
      local_pass = null;
      local_hostname = null;
      local_port = null;
      if ((drive != null) && (file != null)) {
        local_file = drive + file;
      } else if ((path_start != null) && (drive == null) && (file != null)) {
        local_file = path_start + file;
      } else if (drive != null) {
        local_file = drive;
      } else {
        local_file = file;
      }
      valid=true;
      return;
    }

    /*
      //look for a bad port number
      //ftp://(username:pass)*?@hostname(:[0-9]+)*?/.*
      Pattern p_ftp1 = Pattern.compile("(ftp|FTP|sftp|SFTP|http|HTTP|https|HTTPS|webdav|WEBDAV|smb|SMB)://(.*?:.*?@)*(.*?)?([         ]*:[^0-9]+)*?[         ]*[^:]/.*");
      Matcher m_ftp1 = p_ftp1.matcher(_uri);
      if(m_ftp1.matches())return false;

      if (m_file1.matches()) {
        //System.out.println("file matcher");
        protocol = m_file1.group(1);
        drive = m_file1.group(2);
        file = m_file1.group(3);

        //System.out.println("uri="+_uri+"=");
        //System.out.println("file="+file+"=");
        //System.out.println("drive="+drive+"=");
        local_uri = _uri;
        local_protocol = protocol;
        local_user = null;
        local_pass = null;
        local_hostname = null;
        local_port = null;
        if ((drive != null) && (file != null)) {
          local_file = drive + file;
        } else {
          local_file = file;
        }
        return true;
      }

      /*
      //look for a bad port number
      //ftp://(username:pass)*?@hostname(:[0-9]+)*?/.*
      Pattern p_ftp1 = Pattern.compile("(ftp|FTP|sftp|SFTP|http|HTTP|https|HTTPS|webdav|WEBDAV|smb|SMB)://(.*?:.*?@)*(.*?)?([         ]*:[^0-9]+)*?[         ]*[^:]/.*");
      Matcher m_ftp1 = p_ftp1.matcher(_uri);
      if(m_ftp1.matches())return false;
       */

    //remove trailing slash if present
    if (_uri.endsWith("/")) {
      int iend = _uri.length();
      _uri = _uri.substring(0, iend - 1);
      ends_with_slash = true;
    }
    //ftp://(username:pass)*?@hostname(:[0-9]+)*?/.*
    //        "(ftp|FTP|sftp|SFTP|http|HTTP|https|HTTPS|webdav|WEBDAV|smb|SMB)://(.*?:.*?@)*([^:]+)([ 	]*:[0-9]+)*([ 	]*:)*(/.*)");
    //"(ftp|FTP|sftp|SFTP|http|HTTP|https|HTTPS|webdav|WEBDAV|smb|SMB)://(.+:.+@)*([^:]+)([ 	]*:[0-9]+)*([ 	]*:)*(/.*)");
    Pattern p_ftp2 = Pattern
        .compile("(ftp|FTP|sftp|SFTP|http|HTTP|https|HTTPS|webdav|WEBDAV|smb|SMB)://(.+:.+@)*([^:]+?/*)([ 	]*:[0-9]+)*([ 	]*:)*(/.*)");
    Matcher m_ftp2 = p_ftp2.matcher(_uri);

    Pattern p_ftp3 = Pattern
        .compile("(ftp|FTP|sftp|SFTP|http|HTTP|https|HTTPS|webdav|WEBDAV|smb|SMB)://(.+:.+@)*([^:]+)([ 	]*:[0-9]+)*([ 	]*:)*(/*?.*)");
    Matcher m_ftp3 = p_ftp3.matcher(_uri);

    if (m_ftp2.matches()) {
      //System.out.println("ftp2 matcher");
      ret = true;
      protocol = m_ftp2.group(1);
      user_pass = m_ftp2.group(2);
      hostname = m_ftp2.group(3);

      port = m_ftp2.group(4);
      bad_port = m_ftp2.group(5); //this should be null on all valid port inputs
      file = m_ftp2.group(6);
      if (ends_with_slash) {
        file = file + "/";
      }
      if (hostname == null) {
        protocol = null;
        user_pass = null;
        port = null;
        bad_port = null;
        file = null;
        ret = false;
      }

    } else if (m_ftp3.matches()) {
      //System.out.println("ftp3 matcher");
      ret = true;
      protocol = m_ftp3.group(1);
      user_pass = m_ftp3.group(2);
      hostname = m_ftp3.group(3);

      port = m_ftp3.group(4);
      bad_port = m_ftp3.group(5); //this should be null on all valid port inputs
      file = m_ftp3.group(6);
      if (ends_with_slash) {
        file = file + "/";
      }
      if (hostname == null) {
        protocol = null;
        user_pass = null;
        port = null;
        bad_port = null;
        file = null;
        ret = false;
      }
    }


    if ((hostname != null) && hostname.contains("@")) {
      user_pass = hostname.substring(0, hostname.indexOf('@'));
      hostname = hostname.substring(hostname.indexOf('@') + 1);
    }
    //System.out.println("uri="+_uri+"=");
    //System.out.println("protocol="+protocol+"=");
    //System.out.println("user_pass="+user_pass+"=");
    //System.out.println("hostname="+hostname+"=");
    //System.out.println("port="+port+"=");
    //System.out.println("bad_port="+bad_port+"=");
    //System.out.println("file="+file+"=");

    if ((hostname != null)
        && (hostname.startsWith(":") || hostname.endsWith(":") || hostname
        .contains(":"))) {
      //System.out.println("bad hostname="+hostname+"=");
      ret = false;
    }

    if (bad_port != null) {
      //System.out.println("bad_port found="+bad_port+"=");
      ret = false;
    }

    if (!ret) { //don't parse any bad inputs
      valid =false;
      return;
    }
    local_uri = _uri;
    local_protocol = protocol;
    int colon_position;
    if ((user_pass == null) || (user_pass.equals(""))) {
      colon_position = -1;
    } else {
      colon_position = user_pass.indexOf(':');
    }
    if ((user_pass == null) || (user_pass.equals(""))) {
      local_user = null;
      local_pass = null;
    } else if (colon_position == -1) {
      local_user = user_pass;
      local_pass = null;
    } else {
      local_user = user_pass.substring(0, colon_position);
      local_pass = user_pass.substring(colon_position);
    }
    //System.out.println("raw local_pass="+local_pass+"=");
    if (local_pass != null) {
      if (local_pass.endsWith("@")) {
        local_pass = local_pass.substring(0, local_pass.length() - 1);
      }
      if (local_pass.startsWith(":")) {
        local_pass = local_pass.substring(1);
      }
    }
    local_hostname = hostname;
    local_port = port;
    local_file = file;

    valid = true;
  }

  @Override
  public String toString() {
    return "VFSURIValidator{" +
        "local_uri='" + local_uri + '\'' +
        ", local_protocol='" + local_protocol + '\'' +
        ", local_user='" + local_user + '\'' +
        ", local_pass='" + local_pass + '\'' +
        ", local_hostname='" + local_hostname + '\'' +
        ", local_port='" + local_port + '\'' +
        ", local_file='" + local_file + '\'' +
        '}';
  }

  public boolean isValid() {
    return valid;
  }
}
