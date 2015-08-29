package pl.otros.logview.gui.services.jumptocode;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.gui.ConfKeys;
import pl.otros.logview.gui.message.LocationInfo;
import pl.otros.logview.ide.Ide;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * This class is the interface with the JumpToCode plugin for IntellijJ IDEA
 */
public class JumpToCodeClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(JumpToCodeClient.class.getName());
  private final HttpClient client;
  private final Configuration configuration;
  private final Cache<LocationInfo, Boolean> locationInfoCache;
  private final Cache<LocationInfo, String> locationSourceCache;
  private final Cache<String, Ide> ideCache;

  public JumpToCodeClient(Configuration configuration) {
    this.configuration = configuration;
    this.client = new HttpClient();
    //
    client.getHttpConnectionManager().getParams().setConnectionTimeout(150);
    locationInfoCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterAccess(15, TimeUnit.MINUTES).build();
    locationSourceCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(15, TimeUnit.MINUTES).build();
    ideCache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(10, TimeUnit.SECONDS).build();
  }

  public String getContent(final LocationInfo locationInfo) throws IOException {
    try {
      return locationSourceCache.get(locationInfo, () -> {
        HttpMethod httpMethod = buildMethod(getUrl(), locationInfo, HttpOperation.GET_SOURCE);
        return executeAndGetContent(httpMethod);
      });
    } catch (ExecutionException e) {
      return "";
    }
  }

  public Ide getIde(String host, int port) {
    try {
      return new GetIdeCallable(getCheckIdeUrl(host, port)).call();
    } catch (Exception e) {
      return Ide.DISCONNECTED;
    }
  }

  /**
   * get the URL for jumping to the given location
   *
   * @param locationInfo the location
   * @return the JumpToCode url or null when location is unknown
   */
  public String getUrl(LocationInfo locationInfo) {
    if (configuration.getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, true) && isJumpable(locationInfo)) {
      HttpMethod method = buildMethod(getUrl(), locationInfo, HttpOperation.JUMP);
      return String.format("http://%s:%d/?%s",
          configuration.getString(ConfKeys.JUMP_TO_CODE_HOST, "localhost"),
          configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, JumpToCodeService.DEFAULT_PORT),
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
  protected void jumpTo(String url) throws IOException {
    LOGGER.trace("Jumping to URL " + url);
    HttpMethod method = new GetMethod(url);
    int execute = execute(method);
    LOGGER.trace("Result status: " + execute);
  }

  private HttpMethod buildMethod(String url, LocationInfo locationInfo, HttpOperation httpOperation) {
    HttpMethod method = new GetMethod(url);
    NameValuePair[] pair = new NameValuePair[4];
    String lineNumber = String.valueOf(locationInfo.getLineNumber());
    pair[0] = new NameValuePair("p", locationInfo.getPackageName());
    pair[1] = new NameValuePair("f", locationInfo.getFileName());
    pair[2] = new NameValuePair("l", lineNumber);
    pair[3] = new NameValuePair("o", httpOperation.getOperation());

    method.setQueryString(pair);
    return method;
  }

  private String getUrl() {
    String host = configuration.getString(ConfKeys.JUMP_TO_CODE_HOST, JumpToCodeService.DEFAULT_HOST);
    int port = configuration.getInt(ConfKeys.JUMP_TO_CODE_PORT, JumpToCodeService.DEFAULT_PORT);
    return getCheckIdeUrl(host, port);
  }

  private String getCheckIdeUrl(String host, int port) {
    return String.format("http://%s:%d/",
        host,
        port);
  }

  public boolean isJumpable(final LocationInfo locationInfo) {
    if (locationInfo == null) {
      return false;
    }
    try {
      return locationInfoCache.get(locationInfo, () -> {
        LOGGER.trace("Checking if " + locationInfo + " is jumpable.");
        HttpMethod method = buildMethod(getUrl(), locationInfo, HttpOperation.TEST);
        try {
          int execute = execute(method);
          LOGGER.trace("HTTP status line is " + execute);
          return (execute == HttpStatus.SC_OK);
        } catch (IOException e) {
          LOGGER.trace("IOException when checking if location if jumpable", e);
          return false;
        }
      });
    } catch (ExecutionException e) {
      return false;
    }
  }

  private int execute(HttpMethod method) throws IOException {
    try {
      // we don't even read the response body
      // the statusCode is enough information
      return client.executeMethod(method);
    } catch (IOException e) {
      // probably either configuration error or IDEA is not running
      throw new IOException("Failed to communicate with JumpToCode", e);
    } finally {
      method.releaseConnection();
    }
  }

  private String executeAndGetContent(HttpMethod method) throws IOException {
    try {
      // we don't even read the response body
      // the statusCode is enough information
      int status = client.executeMethod(method);
      if (status != HttpStatus.SC_OK) {
        return "";
      }
      return method.getResponseBodyAsString();
    } catch (IOException e) {
      // probably either configuration error or IDEA is not running
      throw new IOException("Failed to communicate with JumpToCode", e);
    } finally {
      method.releaseConnection();
    }
  }

  public Ide getIde() {
    try {
      return ideCache.get(getUrl(), new GetIdeCallable(getUrl()));
    } catch (ExecutionException e) {
      LOGGER.trace("Can't get ide: " + e.getMessage());
    }
    return Ide.DISCONNECTED;
  }

  public void clearLocationCaches() {
    locationInfoCache.invalidateAll();
  }

  private enum HttpOperation {
    JUMP("jump"), TEST("test"), GET_SOURCE("content");
    String operation;

    HttpOperation(String operation) {
      this.operation = operation;
    }

    public String getOperation() {
      return operation;
    }

  }

  private class GetIdeCallable implements Callable<Ide> {
    private final String url;

    private GetIdeCallable(String url) {
      this.url = url;
    }

    @Override
    public Ide call() throws Exception {
      HttpMethod method = buildMethod(url, new LocationInfo("", ""), HttpOperation.TEST);
      Ide ide = Ide.DISCONNECTED;
      try {
        // we don't even read the response body
        // the statusCode is enough information
        client.executeMethod(method);
        Header responseHeader = method.getResponseHeader("IDE");
        if (responseHeader != null) {
          String value = responseHeader.getValue();
          if (StringUtils.equalsIgnoreCase(value, "eclipse")) {
            ide = Ide.Eclipse;
          } else {
            ide = Ide.IDEA;
          }
        }
        return ide;
      } catch (IOException e) {
        // IDE is not available;
        LOGGER.trace("IDE is not available: " + e.getMessage());
        return Ide.DISCONNECTED;
      } finally {
        method.releaseConnection();
      }
    }
  }


}
