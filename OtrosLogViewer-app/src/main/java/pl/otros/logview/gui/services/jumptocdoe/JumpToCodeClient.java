package pl.otros.logview.gui.services.jumptocdoe;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.message.LocationInfo;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This class is the interface with the JumpToCode plugin for IntelliJ IDEA
 */
public class JumpToCodeClient {
  private static final Logger LOGGER = Logger.getLogger(JumpToCodeClient.class.getName());
  private HttpClient client;
  private Configuration configuration;
  private Cache<LocationInfo, Boolean> cache;

  public JumpToCodeClient(Configuration configuration) {
    this.configuration = configuration;
    this.client = new HttpClient();
    cache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterAccess(15, TimeUnit.MINUTES).build();
  }

  /**
   * get the URL for jumping to the given location
   *
   * @param locationInfo the location
   * @return the JumpToCode url or null when location is unknown
   */
  public String getUrl(LocationInfo locationInfo) {
    if (configuration.getBoolean(ConfKeys.JUMP_TO_CODE_ENABLED, true) && isJumpable(locationInfo)) {
      HttpMethod method = buildMethod(locationInfo, false);
      return String.format("http://%s:%d/?%s",
          configuration.getString(ConfKeys.JUMP_TO_CODE_HOST, "localhost"),
          configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, 5986),
          method.getQueryString());
    } else {
      return null;
    }
  }

  /**
   * jump to location represented by given URL
   *
   * @param url a JumpToCode url that represents a source location
   */
  protected void jumpTo(String url) {
    LOGGER.finest("Jumping to URL " + url);
    HttpMethod method = new GetMethod(url);
    int execute = execute(method);
    LOGGER.finest("Result status: " + execute);
  }

  private HttpMethod buildMethod(LocationInfo locationInfo, boolean testOnly) {
    String url = String.format("http://%s:%d/",
        configuration.getString(ConfKeys.JUMP_TO_CODE_HOST, "localhost"),
        configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, 5986));
    HttpMethod method = new GetMethod(url);
    NameValuePair[] pair = testOnly ? new NameValuePair[4] : new NameValuePair[3];
    String lineNumber = String.valueOf(locationInfo.getLineNumber());
    pair[0] = new NameValuePair("p", locationInfo.getPackageName());
    pair[1] = new NameValuePair("f", locationInfo.getFileName());
    pair[2] = new NameValuePair("l", lineNumber);
    if (testOnly) {
      pair[3] = new NameValuePair("o", "test");
    }
    method.setQueryString(pair);
    return method;
  }

  public boolean isJumpable(final LocationInfo locationInfo) {
    if (locationInfo == null) {
      return false;
    }
    try {
      return cache.get(locationInfo, new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          LOGGER.finest("Checking if " + locationInfo + " is jumpable.");
          HttpMethod method = buildMethod(locationInfo, true);
          return (execute(method) == HttpStatus.SC_OK);
        }
      });
    } catch (ExecutionException e) {
      return false;
    }
  }

  private int execute(HttpMethod method) {
    try {
      // we don't even read the response body
      // the statusCode is enough information
      return client.executeMethod(method);
    } catch (IOException e) {
      // probably either configuration error or IDEA is not running
      throw new RuntimeException("Failed to communicate with JumpToCode", e);
    } finally {
      method.releaseConnection();
    }
  }

  public JumpToCodeService.IDE getIde() {
    HttpMethod method = buildMethod(new LocationInfo("", ""), true);
    JumpToCodeService.IDE ide = null;
    try {
      // we don't even read the response body
      // the statusCode is enough information
      client.executeMethod(method);
      Header responseHeader = method.getResponseHeader("IDE");
      if (responseHeader != null) {
        String value = responseHeader.getValue();
        if (StringUtils.equalsIgnoreCase(value, "eclipse")) {
          ide = JumpToCodeService.IDE.Eclipse;
        } else if (StringUtils.equalsIgnoreCase(value, "netbeans")) {
          //TODO netbeans support
//          ide = JumpToCodeService.IDE.Netbeans;
        }
      } else {
        ide = JumpToCodeService.IDE.IDEA;
      }
      return ide;
    } catch (IOException e) {
      // IDE is not available;
      return null;
    } finally {
      method.releaseConnection();
    }
  }
}
