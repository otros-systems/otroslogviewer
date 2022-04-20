package pl.otros.vfs.browser.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(URIUtils.class);

  /**
   * Turns the string representation of a URI into a "friendly URI" by masking the password if the credentials were specified in the URI. The masked password shows as 3 asterisks (***)
   *
   * @param uri the uri to turn into a friendly URI
   * @return null if the <i>URI</i> is null, or the "friendly URI" if a password was found in the <i>uri</i> parameter, otherwise the uri is returned as is.
   */
  public String getFriendlyURI(String uri) {
    String result = null;
    if (uri != null) {
      if (uriContainsCredentials(uri)) {
        String credentials = getCredentials(uri);
        if (credentialsContainPassword(credentials)) {
          String maskedCredentials = maskPassword(credentials);
          result = uri.replace(credentials, maskedCredentials);
        } else {
          result = uri;
        }
      } else {
        result = uri;
      }
    }
    return result;
  }

  private boolean uriContainsCredentials(String uri) {
    return uri.contains("@");
  }

  private String getCredentials(String uri) {
    int initialIndex = uri.indexOf("//");
    if (initialIndex == -1) {
      LOGGER.warn("URI does not contain protocol separator (//), which is unusual. Assuming credentials start at index 0 in the URI.");
      initialIndex = 0;
    }
    return uri.substring(initialIndex, uri.indexOf("@"));
  }

  private boolean credentialsContainPassword(String credentials) {
    return credentials.contains(":");
  }

  private String maskPassword(String credentials) {
    return credentials.substring(0, credentials.indexOf(":")) + ":***";
  }
}
