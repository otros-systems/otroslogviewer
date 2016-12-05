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
public class VFSURIValidator {
  private String local_uri, local_protocol, local_user, local_pass;
  private String local_hostname, local_port, local_file;

  public boolean assertEquals(String _s1, String _s2) {
    if ((_s1 == null) || (_s2 == null)) {
      System.out.println("FATAL assertEquals -- _s1 || _s2 == null");
      System.out.println("_s1=" + _s1 + "=");
      System.out.println("_s2=" + _s2 + "=");
      Exception e = new Exception("");
      e.printStackTrace();
      System.exit(10);
    }
    if (_s1.equals(_s2)) {
    } else {
      System.out.println("FATAL assertEquals -- _s1 != _s2 ");
      System.out.println("_s1=" + _s1 + "=");
      System.out.println("_s2=" + _s2 + "=");
      Exception e = new Exception("");
      e.printStackTrace();
      System.exit(10);
    }
    return false;
  }

  public boolean assertNull(String _s1) {
    if (_s1 != null) {
      System.out.println("FATAL assertNull -- _s1  != null");
      Exception e = new Exception("");
      e.printStackTrace();
      System.exit(10);
    }
    return false;
  }

  public boolean assertnotNull(String _s1) {
    if (_s1 == null) {
      System.out.println("FATAL assertnoNull -- _s1  != null");
      Exception e = new Exception("");
      e.printStackTrace();
      System.exit(10);
    }
    return false;
  }

  public String getUri() {
    if (local_uri.equals(""))
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
      return local_port;
    }
    if (local_port.startsWith(":")) {
      local_port = local_port.substring(1);
    }
    if ((local_port != null) && (local_port.equals("")))
      local_port = null;
    return local_port;
  }

  public String getFile() {
    if ((local_file != null) && (local_file.equals("")))
      local_file = null;
    return local_file;
  }

  public boolean isValid(String _uri) {
    boolean ret = false;
    boolean ends_with_slash = false;

    String protocol = null;
    String user_pass = null;
    String hostname = null;
    String port = null;
    String bad_port = null;
    String drive = null;
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
      } else if ((drive != null) && (file == null)) {
        local_file = drive;
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
    } else {
      //System.out.println("did not match");
    }

    if (ret == true) {
      //leave the various regex parts of the string here in case we want to do more validation/debugging in the future
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

    if (ret == false) { //don't parse any bad inputs
      return ret;
    }
    local_uri = _uri;
    local_protocol = protocol;
    int colon_position = -1;
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

    return ret;
  }

  public void error_msg(String _s) {
    System.out.println("Error in test=" + _s + "=");
    Exception e = new Exception("");
    e.printStackTrace();
    System.exit(10);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //test code
    String s;

    /*
      v.assertEquals(v.getProtocol(),"files");
      v.assertNull(v.getUser());
      v.assertNull(v.getHostname());
      v.assertNull(v.getPassword());
      v.assertNull(v.getPort());
      v.assertEquals(v.getFile(),"c:");
       */
    //unknown protocol names
    s = "files://c:";

    VFSURIValidator v = new VFSURIValidator();

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "files://c:";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "FTPS://c:";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "ftps://c:";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "files123://c:";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "fiLE://c:";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //file tests
    s = "file://c:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "c:");

    s = "file://d:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "d:");

    s = "file://e:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "e:");

    s = "file://z:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "z:");

    s = "file://c:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "c:/");

    s = "file://d:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "d:/");

    s = "file://e:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "e:/");

    s = "file://z:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "z:/");

    s = "file://c:/a";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "c:/a");

    s = "file://d:/a";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "d:/a");

    s = "file://e:/b";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "e:/b");

    s = "file://z:/b";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "z:/b");

    s = "FILE://c:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "c:");

    s = "FILE://d:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "d:");

    s = "FILE://e:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "e:");

    s = "FILE://z:";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "z:");

    s = "FILE://c:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "c:/");

    s = "FILE://d:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "d:/");

    s = "FILE://e:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "e:/");

    s = "FILE://z:/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "z:/");

    s = "FILE://c:/a";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "c:/a");

    s = "FILE://d:/a";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "d:/a");

    s = "FILE://e:/b";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "e:/b");

    s = "FILE://z:/b";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FILE");
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "z:/b");

    //ftp tests
    s = "ftp://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertNull(v.getUser());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "ftp://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertNull(v.getUser());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPassword());
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "ftp://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertNull(v.getUser());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPassword());
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "ftp://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "ftp://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "ftp://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "ftp://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "ftp://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "ftp://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "ftp: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "ftp:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "ftp:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "ftp://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "ftp://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //System.exit(10);
    s = "FTP://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "FTP://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "FTP://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "FTP://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "FTP://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "FTP://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "FTP://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "FTP://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "FTP://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "FTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "FTP: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "FTP:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "FTP:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "FTP://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "FTP://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //sftp tests
    s = "sftp://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "sftp://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "sftp://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "sftp://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "sftp://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "sftp://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "sftp://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "sftp://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "sftp://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "sftp: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "sftp:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "sftp:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "sftp://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "sftp://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SFTP://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SFTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "SFTP://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SFTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "SFTP://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SFTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "SFTP://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SFTP://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SFTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "SFTP://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SFTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "SFTP://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SFTP://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SFTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "SFTP://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SFTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "SFTP: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SFTP:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }

    s = "SFTP:/ /user:pass:@machine";
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SFTP://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SFTP://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //http tests
    s = "http://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "http");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "http://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "http");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "http://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "http");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "http://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "http://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "http");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "http://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "http");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "http://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "http://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "http");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "http://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "http");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "http: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "http:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "http:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "http://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "http://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTP://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTP://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTP://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTP");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTP://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTP://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTP://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTP://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTP://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTP://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTP");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "HTTP: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTP:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTP:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTP://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTP://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //https tests
    s = "https://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "https");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "https://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "https");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "https://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "https");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "https://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "https://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "https");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "https://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "https");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "https://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "https://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "https");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "https://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "https");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "https: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "https:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "https:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "https://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "https://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTPS://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTPS");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTPS://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTPS");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTPS://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTPS");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTPS://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTPS://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTPS");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTPS://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTPS");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTPS://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTPS://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTPS");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "HTTPS://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "HTTPS");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "HTTPS: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTPS:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTPS:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTPS://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "HTTPS://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //webdav tests
    s = "webdav://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "webdav");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "webdav://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "webdav");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "webdav://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "webdav");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "webdav://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "webdav://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "webdav");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "webdav://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "webdav");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "webdav://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "webdav://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "webdav");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "webdav://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }

    s = "webdav: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "webdav:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "webdav:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "webdav://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "webdav://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "WEBDAV://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "WEBDAV");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "WEBDAV://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "WEBDAV");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "WEBDAV://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "WEBDAV");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "WEBDAV://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "WEBDAV://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "WEBDAV");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "WEBDAV://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "WEBDAV");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "WEBDAV://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "WEBDAV://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "WEBDAV");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "WEBDAV://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "WEBDAV");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "WEBDAV: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "WEBDAV:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "WEBDAV:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "WEBDAV://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "WEBDAV://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //smb tests
    s = "smb://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "smb");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "smb://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "smb");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "smb://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "smb");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "smb://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "smb://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "smb");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "smb://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "smb");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "smb://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "smb://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "smb");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "smb://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "smb");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "smb: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "smb:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "smb:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "smb://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "smb://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SMB://machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SMB");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "SMB://machine:1/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SMB");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "1");
    v.assertEquals(v.getFile(), "/the_file");

    s = "SMB://machine:12345/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SMB");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "12345");
    v.assertEquals(v.getFile(), "/the_file");

    s = "SMB://machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SMB://user:pass@machine/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SMB");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "SMB://user:pass@machine:123/the_file";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SMB");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass");
    v.assertEquals(v.getHostname(), "machine");
    v.assertEquals(v.getPort(), "123");
    v.assertEquals(v.getFile(), "/the_file");

    s = "SMB://user:pass@machine:/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SMB://user:pass:@machine/the_file"; //can ":" be part of a password?

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SMB");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_file");

    s = "SMB://user:pass:@machine/the_dir/";

    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "SMB");
    v.assertEquals(v.getUser(), "user");
    v.assertEquals(v.getPassword(), "pass:");
    v.assertEquals(v.getHostname(), "machine");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/the_dir/");

    s = "SMB: //user:pass:@machine/the_file"; //failure tests

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SMB:/ /user:pass:@machine/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SMB:/ /user:pass:@machine";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SMB://user:pass:@:123/a";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "SMB://user:pass:@machine:a/the_file";

    if (v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertNull(v.getProtocol());
    v.assertNull(v.getUser());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPassword());
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    //add tests from Yves
    s = "sftp://shell.sf.net";
    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "shell.sf.net");
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "file:///C:/home/birdman";
    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "C:/home/birdman");

    s = "file:///home/birdman";
    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/home/birdman");

    s = "file://home/birdman";
    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "file");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertNull(v.getHostname());
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "home/birdman");

    s = "webdav://myserver.net/home/yves";
    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "webdav");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "myserver.net");
    v.assertNull(v.getPort());
    v.assertEquals(v.getFile(), "/home/yves");

    s = "ftp://ftp.ca.freebsd.org";
    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "ftp");
    v.assertNull(v.getUser());
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "ftp.ca.freebsd.org");
    v.assertNull(v.getPort());
    v.assertNull(v.getFile());

    s = "sftp://yves@shell.sf.net:28";
    if (!v.isValid(s)) {
      v.error_msg(s);
    }
    v.assertEquals(v.getProtocol(), "sftp");
    v.assertEquals(v.getUser(), "yves");
    v.assertNull(v.getPassword());
    v.assertEquals(v.getHostname(), "shell.sf.net");
    v.assertEquals(v.getPort(), "28");
    v.assertNull(v.getFile());

    System.out.println("all done");
  }
}
