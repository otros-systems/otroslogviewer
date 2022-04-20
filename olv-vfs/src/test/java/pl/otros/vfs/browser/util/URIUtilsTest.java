package pl.otros.vfs.browser.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class URIUtilsTest {
  private URIUtils classUnderTest;

  @Before
  public void setUp() {
    classUnderTest = new URIUtils();
  }

  @Test
  public void testGetFriendlyURI_handlesNullURI() {
    Assert.assertNull("Expected null when URI parameter is null", classUnderTest.getFriendlyURI(null));
  }

  @Test
  public void testGetFriendlyURI_handlesURINoCredentials() {
    String uri = "sftp://hostname:21/path/file.log";
    String actual = classUnderTest.getFriendlyURI(uri);
    Assert.assertEquals("Expected the same uri", uri, actual);
  }

  @Test
  public void testGetFriendlyURI_handlesURIWithCredentials() {
    String uri = "sftp://username:password@hostname:21/path/file.log";
    String expected = "sftp://username:***@hostname:21/path/file.log";
    String actual = classUnderTest.getFriendlyURI(uri);
    Assert.assertEquals("Expected masked password in URI", expected, actual);
    Assert.assertTrue("Password should be replaced with 3 asterisks", actual.contains(":***@"));
  }


  @Test
  public void testGetFriendlyURI_handlesURIWithUsernameNoPassword() {
    String uri = "sftp://username@hostname:21/path/file.log";
    String actual = classUnderTest.getFriendlyURI(uri);
    Assert.assertEquals("Expected masked password in URI", uri, actual);
  }

  @Test
  public void testGetFriendlyURI_handlesMalformedURI() {
    String uri = "sftp:username:password@hostname:21/path/file.log";
    String expected = "sftp:***@hostname:21/path/file.log";
    String actual = classUnderTest.getFriendlyURI(uri);
    Assert.assertEquals("Expected 'sftp' to be recognized as the username, and the actual credentials to be masked", expected, actual);
  }

}