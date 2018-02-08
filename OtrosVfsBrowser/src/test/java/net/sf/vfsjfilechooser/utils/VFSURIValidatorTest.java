package net.sf.vfsjfilechooser.utils;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class VFSURIValidatorTest {


  @Test(dataProvider = "validData")
  public void testIsValid(String url,
                          String protocol,
                          String user,
                          String password,
                          String hostname,
                          String port,
                          String file
  ) throws Exception {
    final VFSURIValidator v = VFSURIValidator.validate(url);
    assertTrue(v.isValid(), url + " should be valid");
    assertEquals(v.getProtocol(), protocol, url);
    assertEquals(v.getUser(), user, url);
    assertEquals(v.getPassword(), password, url);
    assertEquals(v.getHostname(), hostname, url);
    assertEquals(v.getPort(), port, url);
    assertEquals(v.getFile(), file, url);

  }

  @Test(dataProvider = "invalidData")
  public void testIsInValid(String url) throws Exception {
    final VFSURIValidator v = VFSURIValidator.validate(url);
    Assert.assertFalse(v.isValid(), url + " should be invalid");

  }

  private Object[][] validData = {
      new Object[]{"FILE://c:", "FILE", null, null, null, null, "c:"},
      new Object[]{"FILE://c:/", "FILE", null, null, null, null, "c:/"},
      new Object[]{"FILE://c:/a", "FILE", null, null, null, null, "c:/a"},
      new Object[]{"FILE://d:", "FILE", null, null, null, null, "d:"},
      new Object[]{"FILE://d:/", "FILE", null, null, null, null, "d:/"},
      new Object[]{"FILE://d:/a", "FILE", null, null, null, null, "d:/a"},
      new Object[]{"FILE://e:", "FILE", null, null, null, null, "e:"},
      new Object[]{"FILE://e:/", "FILE", null, null, null, null, "e:/"},
      new Object[]{"FILE://e:/b", "FILE", null, null, null, null, "e:/b"},
      new Object[]{"FILE://z:", "FILE", null, null, null, null, "z:"},
      new Object[]{"FILE://z:/", "FILE", null, null, null, null, "z:/"},
      new Object[]{"FILE://z:/b", "FILE", null, null, null, null, "z:/b"},

      new Object[]{"FTP://machine/the_file", "FTP", null, null, "machine", null, "/the_file"},
      new Object[]{"FTP://machine:1/the_file", "FTP", null, null, "machine", "1", "/the_file"},
      new Object[]{"FTP://machine:12345/the_file", "FTP", null, null, "machine", "12345", "/the_file"},
      new Object[]{"FTP://user:pass:@machine/the_dir/", "FTP", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"FTP://user:pass:@machine/the_file", "FTP", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,true,
      // "FTP", "user", "pass", "machine",null,  "/the_file" },
      new Object[]{"FTP://user:pass@machine/the_file", "FTP", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"FTP://user:pass@machine:123/the_file", "FTP", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"HTTP://machine/the_file", "HTTP", null, null, "machine", null, "/the_file"},
      new Object[]{"HTTP://machine:1/the_file", "HTTP", null, null, "machine", "1", "/the_file"},
      new Object[]{"HTTP://machine:12345/the_file", "HTTP", null, null, "machine", "12345", "/the_file"},
      new Object[]{"HTTP://user:pass:@machine/the_dir/", "HTTP", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"HTTP://user:pass:@machine/the_file", "HTTP", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,true,
      // "HTTP", "pass", "user", "machine",null,  "/the_file" },
      new Object[]{"HTTP://user:pass@machine/the_file", "HTTP", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"HTTP://user:pass@machine:123/the_file", "HTTP", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"HTTPS://machine/the_file", "HTTPS", null, null, "machine", null, "/the_file"},
      new Object[]{"HTTPS://machine:1/the_file", "HTTPS", null, null, "machine", "1", "/the_file"},
      new Object[]{"HTTPS://machine:12345/the_file", "HTTPS", null, null, "machine", "12345", "/the_file"},
      new Object[]{"HTTPS://user:pass:@machine/the_dir/", "HTTPS", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"HTTPS://user:pass:@machine/the_file", "HTTPS", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,
      // true, "HTTPS", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"HTTPS://user:pass@machine/the_file", "HTTPS", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"HTTPS://user:pass@machine:123/the_file", "HTTPS", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"SFTP://machine/the_file", "SFTP", null, null, "machine", null, "/the_file"},
      new Object[]{"SFTP://machine:1/the_file", "SFTP", null, null, "machine", "1", "/the_file"},
      new Object[]{"SFTP://machine:12345/the_file", "SFTP", null, null, "machine", "12345", "/the_file"},
      new Object[]{"SFTP://user:pass:@machine/the_dir/", "SFTP", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"SFTP://user:pass:@machine/the_file", "SFTP", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,true,
      // "SFTP", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"SFTP://user:pass@machine/the_file", "SFTP", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"SFTP://user:pass@machine:123/the_file", "SFTP", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"SMB://machine/the_file", "SMB", null, null, "machine", null, "/the_file"},
      new Object[]{"SMB://machine:1/the_file", "SMB", null, null, "machine", "1", "/the_file"},
      new Object[]{"SMB://machine:12345/the_file", "SMB", null, null, "machine", "12345", "/the_file"},
      new Object[]{"SMB://user:pass:@machine/the_dir/", "SMB", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"SMB://user:pass:@machine/the_file", "SMB", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,true,
      // "SMB", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"SMB://user:pass@machine/the_file", "SMB", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"SMB://user:pass@machine:123/the_file", "SMB", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"WEBDAV://machine/the_file", "WEBDAV", null, null, "machine", null, "/the_file"},
      new Object[]{"WEBDAV://machine:1/the_file", "WEBDAV", null, null, "machine", "1", "/the_file"},
      new Object[]{"WEBDAV://machine:12345/the_file", "WEBDAV", null, null, "machine", "12345", "/the_file"},
      new Object[]{"WEBDAV://user:pass:@machine/the_dir/", "WEBDAV", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"WEBDAV://user:pass:@machine/the_file", "WEBDAV", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,
      // true, "WEBDAV", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"WEBDAV://user:pass@machine/the_file", "WEBDAV", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"WEBDAV://user:pass@machine:123/the_file", "WEBDAV", "user", "pass", "machine", "123", "/the_file"},


      new Object[]{"file:///C:/home/birdman", "file", null, null, null, null, "C:/home/birdman"},
      new Object[]{"file:///home/birdman", "file", null, null, null, null, "/home/birdman"},
      new Object[]{"file://c:", "file", null, null, null, null, "c:"},
      new Object[]{"file://c:/", "file", null, null, null, null, "c:/"},
      new Object[]{"file://c:/a", "file", null, null, null, null, "c:/a"},

      new Object[]{"ftp://ftp.ca.freebsd.org", "ftp", null, null, "ftp.ca.freebsd.org", null, null},
      new Object[]{"ftp://machine/the_file", "ftp", null, null, "machine", null, "/the_file"},
      new Object[]{"ftp://machine:1/the_file", "ftp", null, null, "machine", "1", "/the_file"},
      new Object[]{"ftp://machine:12345/the_file", "ftp", null, null, "machine", "12345", "/the_file"},
      new Object[]{"ftp://user:pass:@machine/the_dir/", "ftp", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"ftp://user:pass:@machine/the_file", "ftp", "user", "pass:", "machine", null, "/the_file"},     //can ":" be part of a password?,true,
      // "ftp", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"ftp://user:pass@machine/the_file", "ftp", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"ftp://user:pass@machine:123/the_file", "ftp", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"http://machine/the_file", "http", null, null, "machine", null, "/the_file"},
      new Object[]{"http://machine:1/the_file", "http", null, null, "machine", "1", "/the_file"},
      new Object[]{"http://machine:12345/the_file", "http", null, null, "machine", "12345", "/the_file"},
      new Object[]{"http://user:pass:@machine/the_dir/", "http", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"http://user:pass:@machine/the_file", "http", "user", "pass:", "machine", null, "/the_file"},     //can ":" be part of a password?,true,
      // "http", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"http://user:pass@machine/the_file", "http", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"http://user:pass@machine:123/the_file", "http", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"https://machine/the_file", "https", null, null, "machine", null, "/the_file"},
      new Object[]{"https://machine:1/the_file", "https", null, null, "machine", "1", "/the_file"},
      new Object[]{"https://machine:12345/the_file", "https", null, null, "machine", "12345", "/the_file"},
      new Object[]{"https://user:pass:@machine/the_dir/", "https", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"https://user:pass:@machine/the_file", "https", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,
      // true, "https", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"https://user:pass@machine/the_file", "https", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"https://user:pass@machine:123/the_file", "https", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"sftp://machine/the_file", "sftp", null, null, "machine", null, "/the_file"},
      new Object[]{"sftp://machine:1/the_file", "sftp", null, null, "machine", "1", "/the_file"},
      new Object[]{"sftp://machine:12345/the_file", "sftp", null, null, "machine", "12345", "/the_file"},
      new Object[]{"sftp://shell.sf.net", "sftp", null, null, "shell.sf.net", null, null},
      new Object[]{"sftp://user:pass:@machine/the_dir/", "sftp", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"sftp://user:pass:@machine/the_file", "sftp", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,true,
      // "sftp", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"sftp://user:pass@machine/the_file", "sftp", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"sftp://user:pass@machine:123/the_file", "sftp", "user", "pass", "machine", "123", "/the_file"},
      new Object[]{"sftp://yves@shell.sf.net:28", "sftp", "yves", null, "shell.sf.net", "28", null},

      new Object[]{"smb://machine/the_file", "smb", null, null, "machine", null, "/the_file"},
      new Object[]{"smb://machine:1/the_file", "smb", null, null, "machine", "1", "/the_file"},
      new Object[]{"smb://machine:12345/the_file", "smb", null, null, "machine", "12345", "/the_file"},
      new Object[]{"smb://user:pass:@machine/the_dir/", "smb", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"smb://user:pass:@machine/the_file", "smb", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,true,
      // "smb", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"smb://user:pass@machine/the_file", "smb", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"smb://user:pass@machine:123/the_file", "smb", "user", "pass", "machine", "123", "/the_file"},

      new Object[]{"webdav://machine/the_file", "webdav", null, null, "machine", null, "/the_file"},
      new Object[]{"webdav://machine:1/the_file", "webdav", null, null, "machine", "1", "/the_file"},
      new Object[]{"webdav://machine:12345/the_file", "webdav", null, null, "machine", "12345", "/the_file"},
      new Object[]{"webdav://myserver.net/home/yves", "webdav", null, null, "myserver.net", null, "/home/yves"},
      new Object[]{"webdav://user:pass:@machine/the_dir/", "webdav", "user", "pass:", "machine", null, "/the_dir/"},
      new Object[]{"webdav://user:pass:@machine/the_file", "webdav", "user", "pass:", "machine", null, "/the_file"},      //can ":" be part of a password?,
      // true, "webdav", "user", "pass", "machine",null,  "FILE" },
      new Object[]{"webdav://user:pass@machine/the_file", "webdav", "user", "pass", "machine", null, "/the_file"},
      new Object[]{"webdav://user:pass@machine:123/the_file", "webdav", "user", "pass", "machine", "123", "/the_file"},
  };

  private Object[][] invalidData = {
      new Object[]{"WEBDAV: //user:pass:@machine/the_file"}, //failure tests,true}
      new Object[]{"SMB: //user:pass:@machine/the_file"},//failure tests,true},
      new Object[]{"SFTP: //user:pass:@machine/the_file"},//failure tests,true},
      new Object[]{"HTTPS: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"FTP: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"FTP:/ /user:pass:@machine"},
      new Object[]{"FTP:/ /user:pass:@machine/the_file"},
      new Object[]{"FTP://machine:/the_file"},
      new Object[]{"FTP://user:pass:@:123/a"},
      new Object[]{"FTP://user:pass:@machine:a/the_file"},
      new Object[]{"FTP://user:pass@machine:/the_file"},
      new Object[]{"FTPS://c:"},
      new Object[]{"HTTP: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"HTTP:/ /user:pass:@machine"},
      new Object[]{"HTTP:/ /user:pass:@machine/the_file"},
      new Object[]{"HTTP://machine:/the_file"},
      new Object[]{"HTTP://user:pass:@:123/a"},
      new Object[]{"HTTP://user:pass:@machine:a/the_file"},
      new Object[]{"HTTP://user:pass@machine:/the_file"},
      new Object[]{"HTTPS: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"HTTPS:/ /user:pass:@machine"},
      new Object[]{"HTTPS:/ /user:pass:@machine/the_file"},
      new Object[]{"HTTPS://machine:/the_file"},
      new Object[]{"HTTPS://user:pass:@:123/a"},
      new Object[]{"HTTPS://user:pass:@machine:a/the_file"},
      new Object[]{"HTTPS://user:pass@machine:/the_file"},
      new Object[]{"SFTP: //user:pass:@machine/the_file"},//failure tests,true},
      new Object[]{"SFTP:/ /user:pass:@machine"},
      new Object[]{"SFTP:/ /user:pass:@machine/the_file"},
      new Object[]{"SFTP://machine:/the_file"},
      new Object[]{"SFTP://user:pass:@:123/a"},
      new Object[]{"SFTP://user:pass:@machine:a/the_file"},
      new Object[]{"SFTP://user:pass@machine:/the_file"},
      new Object[]{"SMB: //user:pass:@machine/the_file"},//failure tests,true},
      new Object[]{"SMB:/ /user:pass:@machine"},
      new Object[]{"SMB:/ /user:pass:@machine/the_file"},
      new Object[]{"SMB://machine:/the_file"},
      new Object[]{"SMB://user:pass:@:123/a"},
      new Object[]{"SMB://user:pass:@machine:a/the_file"},
      new Object[]{"SMB://user:pass@machine:/the_file"},
      new Object[]{"WEBDAV: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"WEBDAV:/ /user:pass:@machine"},
      new Object[]{"WEBDAV:/ /user:pass:@machine/the_file"},
      new Object[]{"WEBDAV://machine:/the_file"},
      new Object[]{"WEBDAV://user:pass:@:123/a"},
      new Object[]{"WEBDAV://user:pass:@machine:a/the_file"},
      new Object[]{"WEBDAV://user:pass@machine:/the_file"},
      new Object[]{"fiLE://c:"},
      new Object[]{"files123://c:"},
      new Object[]{"files://c:"},
      new Object[]{"ftp: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"ftp:/ /user:pass:@machine"},
      new Object[]{"ftp:/ /user:pass:@machine/the_file"},
      new Object[]{"ftp://machine:/the_file"},
      new Object[]{"ftp://user:pass:@:123/a"},
      new Object[]{"ftp://user:pass:@machine:a/the_file"},
      new Object[]{"ftp://user:pass@machine:/the_file"},
      new Object[]{"ftps://c:"},
      new Object[]{"http: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"http:/ /user:pass:@machine"},
      new Object[]{"http:/ /user:pass:@machine/the_file"},
      new Object[]{"http://machine:/the_file"},
      new Object[]{"http://user:pass:@:123/a"},
      new Object[]{"http://user:pass:@machine:a/the_file"},
      new Object[]{"http://user:pass@machine:/the_file"},
      new Object[]{"https: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"https:/ /user:pass:@machine"},
      new Object[]{"https:/ /user:pass:@machine/the_file"},
      new Object[]{"https://machine:/the_file"},
      new Object[]{"https://user:pass:@:123/a"},
      new Object[]{"https://user:pass:@machine:a/the_file"},
      new Object[]{"https://user:pass@machine:/the_file"},
      new Object[]{"sftp: //user:pass:@machine/the_file"},      //failure tests,true},
      new Object[]{"sftp:/ /user:pass:@machine"},
      new Object[]{"sftp:/ /user:pass:@machine/the_file"},
      new Object[]{"sftp://machine:/the_file"},
      new Object[]{"sftp://user:pass:@:123/a"},
      new Object[]{"sftp://user:pass:@machine:a/the_file"},
      new Object[]{"sftp://user:pass@machine:/the_file"},
      new Object[]{"smb: //user:pass:@machine/the_file"},//failure tests,true},
      new Object[]{"smb:/ /user:pass:@machine"},
      new Object[]{"smb:/ /user:pass:@machine/the_file"},
      new Object[]{"smb://machine:/the_file"},
      new Object[]{"smb://user:pass:@:123/a"},
      new Object[]{"smb://user:pass:@machine:a/the_file"},
      new Object[]{"smb://user:pass@machine:/the_file"},
      new Object[]{"webdav: //user:pass:@machine/the_file"},//failure tests,true},
      new Object[]{"webdav:/ /user:pass:@machine"},
      new Object[]{"webdav:/ /user:pass:@machine/the_file"},
      new Object[]{"webdav://machine:/the_file"},
      new Object[]{"webdav://user:pass:@:123/a"},
      new Object[]{"webdav://user:pass:@machine:a/the_file"},
      new Object[]{"webdav://user:pass@machine:/the_file"},
      new Object[]{"files://c:"},
  };

  @DataProvider(name = "validData")
  public Object[][] validationData() {
    return validData;
  }

  @DataProvider(name = "invalidData")
  public Object[][] invalidData() {
    return invalidData;
  }

}