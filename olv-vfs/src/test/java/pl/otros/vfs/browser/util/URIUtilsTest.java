package pl.otros.vfs.browser.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class URIUtilsTest {
    private final URIUtils classUnderTest = new URIUtils();

    @Test
    public void testGetFriendlyURI_handlesNullURI() {
        Assert.assertNull(classUnderTest.getFriendlyURI(null), "Expected null when URI parameter is null");
    }

    @Test
    public void testGetFriendlyURI_handlesURINoCredentials() {
        String uri = "sftp://hostname:21/path/file.log";
        String actual = classUnderTest.getFriendlyURI(uri);
        Assert.assertEquals(actual, uri, "Expected the same uri");
    }

    @Test
    public void testGetFriendlyURI_handlesURIWithCredentials() {
        String uri = "sftp://username:password@hostname:21/path/file.log";
        String expected = "sftp://username:***@hostname:21/path/file.log";
        String actual = classUnderTest.getFriendlyURI(uri);
        Assert.assertEquals(actual, expected, "Expected masked password in URI");
        Assert.assertTrue(actual.contains(":***@"), "Password should be replaced with 3 asterisks");
    }

    @Test
    public void testGetFriendlyURI_handlesURIWithUsernameNoPassword() {
        String uri = "sftp://username@hostname:21/path/file.log";
        String actual = classUnderTest.getFriendlyURI(uri);
        Assert.assertEquals(actual, uri, "Expected masked password in URI");
    }

    @Test
    public void testGetFriendlyURI_handlesMalformedURI() {
        String uri = "sftp:username:password@hostname:21/path/file.log";
        String expected = "sftp:***@hostname:21/path/file.log";
        String actual = classUnderTest.getFriendlyURI(uri);
        Assert.assertEquals(actual, expected, "Expected 'sftp' to be recognized as the username, and the actual credentials to be masked");
    }

}